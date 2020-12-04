package configgen.gencs;

import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.ForeignKey;
import configgen.gen.*;
import configgen.type.*;
import configgen.util.CachedFiles;
import configgen.util.CachedIndentPrinter;
import configgen.value.AllValue;
import configgen.value.VTable;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenCs extends Generator {

    private final String dir;
    private final String pkg;
    private final String encoding;
    private final String prefix;
    private final String own;
    private File dstDir;
    private AllValue value;

    public GenCs(Parameter parameter) {
        super(parameter);
        dir = parameter.get("dir", "Config", "目录");
        pkg = parameter.get("pkg", "Config", "包名");
        encoding = parameter.get("encoding", "GBK", "生成文件的编码");
        prefix = parameter.get("prefix", "Data", "生成类的前缀");
        own = parameter.get("own", null, "提取部分配置");
        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        dstDir = Paths.get(dir).resolve(pkg.replace('.', '/')).toFile();
        value = ctx.makeValue(own);
        //copyFile("CSV.cs");
        //copyFile("CSVLoader.cs");
        //copyFile("LoadErrors.cs");
        //copyFile("KeyedList.cs");
        genCSVProcessor();

        for (TBean tbean : value.getTDb().getTBeans()) {
            generateBeanClass(tbean, null);

            for (TBean childBean : tbean.getChildDynamicBeans()) {
                generateBeanClass(childBean, null);
            }
        }
        for (VTable vtable : value.getVTables()) {
            generateBeanClass(vtable.getTTable().getTBean(), vtable);
        }

        CachedFiles.keepMetaAndDeleteOtherFiles(dstDir);
    }

    private static class Name {
        final String pkg;
        final String className;
        final String fullName;
        final String path;

        Name(String topPkg, String prefix, TBean tbean) {
            String name;
            if (tbean.getBeanDefine().type == Bean.BeanType.ChildDynamicBean) {
                TBean baseAction = (TBean) tbean.parent;
                name = baseAction.name.toLowerCase() + "." + tbean.name;
            } else {
                name = tbean.name;
            }
            String[] seps = name.split("\\.");
            String[] pks = new String[seps.length - 1];
            for (int i = 0; i < pks.length; i++)
                pks[i] = upper1Only(seps[i]);
            className = prefix + upper1Only(seps[seps.length - 1]);

            if (pks.length == 0)
                pkg = topPkg;
            else
                pkg = topPkg + "." + String.join(".", pks);

            if (pkg.isEmpty())
                fullName = className;
            else
                fullName = pkg + "." + className;

            if (pks.length == 0)
                path = className + ".cs";
            else
                path = String.join("/", pks) + "/" + className + ".cs";
        }
    }

    private void generateBeanClass(TBean tbean, VTable vtable) throws IOException {
        Name name = new Name(pkg, prefix, tbean);
        File csFile = dstDir.toPath().resolve(name.path).toFile();
        try (CachedIndentPrinter ps = createCode(csFile, encoding)) {
            if (tbean.getBeanDefine().type == Bean.BeanType.BaseDynamicBean) {
                generateBaseActionClass(tbean, name, ps);
            } else {
                generateBeanClass(tbean, vtable, name, ps);
            }
        }
    }

    private void generateBaseActionClass(TBean tbean, Name name, CachedIndentPrinter ps) {
        ps.println("using System;");
        ps.println("using System.Collections.Generic;");
        ps.println("using System.IO;");
        if (!pkg.equals("Config")) {
            ps.println("using Config;");
        }
        ps.println("namespace " + name.pkg);
        ps.println("{");
        ps.println("public abstract class " + name.className);
        ps.println("{");
        ps.println1("public abstract " + fullName(tbean.getChildDynamicBeanEnumRefTable()) + " type();");
        ps.println();

        if (tbean.hasRef()) {
            ps.println1("internal virtual void _resolve(Config.LoadErrors errors)");
            ps.println1("{");
            ps.println1("}");
            ps.println();
        }

        ps.println1("internal static " + name.className + " _create(Config.Stream os) {");
        ps.println2("switch(os.ReadString()) {");
        for (TBean actionBean : tbean.getChildDynamicBeans()) {
            ps.println3("case \"" + actionBean.name + "\":");
            ps.println4("return " + fullName(actionBean) + "._create(os);");
        }
        ps.println2("}");
        ps.println2("return null;");
        ps.println1("}");
        ps.println("}");
        ps.println("}");
    }

    private void generateBeanClass(TBean tbean, VTable vtable, Name name, CachedIndentPrinter ps) {
        TTable ttable = vtable != null ? vtable.getTTable() : null;
        ps.println("using System;");
        ps.println("using System.Collections.Generic;");
        ps.println("using System.IO;");
        if (!pkg.equals("Config")) {
            ps.println("using Config;");
        }
        ps.println();

        ps.println("namespace " + name.pkg);
        ps.println("{");

        boolean isAction = tbean.getBeanDefine().type == Bean.BeanType.ChildDynamicBean;
        if (isAction) {
            TBean baseAction = (TBean) tbean.parent;
            ps.println1("public partial class " + name.className + " : " + fullName(baseAction));
            ps.println1("{");
            ps.println2("public override " + fullName(baseAction.getChildDynamicBeanEnumRefTable()) + " type() {");
            ps.println3("return " + fullName(baseAction.getChildDynamicBeanEnumRefTable()) + "." + tbean.name + ";");
            ps.println2("}");
            ps.println();
        } else {
            ps.println1("public partial class " + name.className);
            ps.println1("{");
        }


        //static enum
        if (ttable != null && ttable.getTableDefine().isEnum()) {
            vtable.getEnumNames().forEach(e -> ps.println2("public static " + name.className + " " + upper1(e) + " { get; private set; }"));
            ps.println();
        }

        // property
        tbean.getColumnMap().forEach((n, t) -> {
            Column f = tbean.getBeanDefine().columns.get(n);
            String c = f.desc.isEmpty() ? "" : " /* " + f.desc + "*/";
            ps.println2("public " + type(t) + " " + upper1(n) + " { get; private set; }" + c);
            t.getConstraint().references.forEach(r -> ps.println2("public " + refType(t, r) + " " + refName(r) + " { get; private set; }"));
        });

        tbean.getMRefs().forEach(m -> ps.println2("public " + fullName(m.refTable) + " " + refName(m) + " { get; private set; }"));
        tbean.getListRefs().forEach(l -> ps.println2("public List<" + fullName(l.refTable) + "> " + refName(l) + " { get; private set; }"));
        ps.println();

        //constructor
        if (ttable == null) {
            ps.println2("public " + name.className + "() {");
            ps.println2("}");
            ps.println();

            ps.println2("public " + name.className + "(" + formalParams(tbean.getColumnMap()) + ") {");
            tbean.getColumnMap().forEach((n, t) -> ps.println3("this." + upper1(n) + " = " + lower1(n) + ";"));
            ps.println2("}");
            ps.println();
        }

        //hash
        Map<String, Type> keys = ttable != null ? ttable.getPrimaryKey() : tbean.getColumnMap();
        ps.println2("public override int GetHashCode()");
        ps.println2("{");
        ps.println3("return " + hashCodes(keys) + ";");
        ps.println2("}");
        ps.println();

        //equal
        ps.println2("public override bool Equals(object obj)");
        ps.println2("{");
        ps.println3("if (obj == null) return false;");
        ps.println3("if (obj == this) return true;");
        ps.println3("var o = obj as " + name.className + ";");
        ps.println3("return o != null && " + equals(keys) + ";");
        ps.println2("}");
        ps.println();

        //toString
        ps.println2("public override string ToString()");
        ps.println2("{");
        ps.println3("return \"(\" + " + toStrings(tbean.getColumnMap()) + " + \")\";");
        ps.println2("}");
        ps.println();

        String csv = "\"" + tbean.getBeanDefine().name + "\"";
        if (ttable != null) {
            generateMapGetBy(ttable.getPrimaryKey(), name, ps, true);
            for (Map<String, Type> uniqueKey : ttable.getUniqueKeys()) {
                generateMapGetBy(uniqueKey, name, ps, false);
            }

            //static all
            ps.println2("public static List<" + name.className + "> All()");
            ps.println2("{");
            ps.println3("return all.OrderedValues;");
            ps.println2("}");
            ps.println();

            // static filter
            ps.println2("public static List<" + name.className + "> Filter(Predicate<" + name.className + "> predicate)");
            ps.println2("{");
            ps.println3("var r = new List<" + name.className + ">();");
            ps.println3("foreach (var e in all.OrderedValues)");
            ps.println3("{");
            ps.println4("if (predicate(e))");
            ps.println5("r.Add(e);");
            ps.println3("}");
            ps.println3("return r;");
            ps.println2("}");
            ps.println();

            //static initialize
            ps.println2("internal static void Initialize(Config.Stream os, Config.LoadErrors errors)");
            ps.println2("{");
            ps.println3("all = new Config.KeyedList<" + keyClassName(keys) + ", " + name.className + ">();");
            for (Map<String, Type> uniqueKey : ttable.getUniqueKeys()) {
                ps.println3(uniqueKeyMapName(uniqueKey) + " = new Config.KeyedList<" + keyClassName(uniqueKey) + ", " + name.className + ">();");
            }

            ps.println3("for (var c = os.ReadInt32(); c > 0; c--) {");
            ps.println4("var self = _create(os);");
            generateAllMapPut(ttable, ps);

            if (ttable.getTableDefine().isEnum()) {
                String ef = upper1(ttable.getTableDefine().enumStr);
                ps.println4("if (self." + ef + ".Trim().Length == 0)");
                ps.println5("continue;");
                ps.println4("switch(self." + ef + ".Trim())");
                ps.println4("{");
                vtable.getEnumNames().forEach(e -> {
                    ps.println5("case \"" + e + "\":");
                    ps.println6("if (" + upper1(e) + " != null)");
                    ps.println7("errors.EnumDup(" + csv + ", self.ToString());");
                    ps.println6(upper1(e) + " = self;");
                    ps.println6("break;");
                });
                ps.println5("default:");
                ps.println6("errors.EnumDataAdd(" + csv + ", self.ToString());");
                ps.println6("break;");
                ps.println4("}");
            }
            ps.println3("}");

            if (ttable.getTableDefine().isEnum()) {
                vtable.getEnumNames().forEach(e -> {
                    ps.println3("if (" + upper1(e) + " == null)");
                    ps.println4("errors.EnumNull(" + csv + ", \"" + e + "\");");
                });
            }
            ps.println2("}");
            ps.println();

            //static resolve
            if (tbean.hasRef()) {
                ps.println2("internal static void Resolve(Config.LoadErrors errors) {");
                ps.println3("foreach (var v in All())");
                ps.println4("v._resolve(errors);");
                ps.println2("}");
                ps.println();
            }
        } // end cfg != null

        //static create
        String pre = isAction ? "internal new static " : "internal static ";
        ps.println2(pre + name.className + " _create(Config.Stream os)");
        ps.println2("{");
        ps.println3("var self = new " + name.className + "();");
        tbean.getColumnMap().forEach((n, t) -> {
            if (t instanceof TList) {
                ps.println3("self." + upper1(n) + " = new " + type(t) + "();");
                ps.println3("for (var c = os.ReadInt32(); c > 0; c--)");
                ps.println4("self." + upper1(n) + ".Add(" + _create(((TList) t).value) + ");");
            } else if (t instanceof TMap) {
                ps.println3("self." + upper1(n) + " = new " + type(t) + "();");
                ps.println3("for (var c = os.ReadInt32(); c > 0; c--)");
                ps.println4("self." + upper1(n) + ".Add(" + _create(((TMap) t).key) + ", " + _create(((TMap) t).value) + ");");
            } else {
                ps.println3("self." + upper1(n) + " = " + _create(t) + ";");
            }
        });
        ps.println3("return self;");
        ps.println2("}");
        ps.println();

        //resolve
        if (tbean.hasRef()) {
            pre = isAction ? "internal override " : "internal ";
            ps.println2(pre + "void _resolve(Config.LoadErrors errors)");
            ps.println2("{");
            tbean.getColumnMap().forEach((n, t) -> {
                if (t.hasRef()) {
                    String field = "\"" + n + "\"";
                    if (t instanceof TList) {
                        TList tt = (TList) t;
                        if (tt.value instanceof TBeanRef && tt.value.hasRef()) {
                            ps.println3("foreach (var e in " + upper1(n) + ")");
                            ps.println3("{");
                            ps.println4("e._resolve(errors);");
                            ps.println3("}");
                        }

                        for (SRef sr : t.getConstraint().references) {
                            ps.println3(refName(sr) + " = new " + refType(t, sr) + "();");
                            ps.println3("foreach (var e in " + upper1(n) + ")");
                            ps.println3("{");
                            ps.println4("var r = " + tableGet(sr.refTable, sr.refCols, "e"));
                            ps.println4("if (r == null) errors.RefNull(" + csv + ", ToString() , " + field + ", e);");
                            ps.println4(refName(sr) + ".Add(r);");
                            ps.println3("}");
                        }
                    } else if (t instanceof TMap) {
                        TMap tt = (TMap) t;
                        if ((tt.key instanceof TBeanRef && tt.key.hasRef()) || (tt.value instanceof TBeanRef && tt.value.hasRef())) {
                            ps.println3("foreach (var kv in " + upper1(n) + ".Map)");
                            ps.println3("{");
                            if (tt.key instanceof TBeanRef && tt.key.hasRef()) {
                                ps.println4("kv.Key._resolve(errors);");
                            }
                            if (tt.value instanceof TBeanRef && tt.value.hasRef()) {
                                ps.println4("kv.Value._resolve(errors);");
                            }
                            ps.println3("}");
                        }
                        for (SRef sr : t.getConstraint().references) {
                            ps.println3(refName(sr) + " = new " + refType(t, sr) + "();");
                            ps.println3("foreach (var kv in " + upper1(n) + ".Map)");
                            ps.println3("{");

                            if (sr.mapKeyRefTable != null) {
                                ps.println4("var k = " + tableGet(sr.mapKeyRefTable, sr.mapKeyRefCols, "kv.Key"));
                                ps.println4("if (k == null) errors.RefKeyNull(" + csv + ", ToString(), " + field + ", kv.Key);");
                            } else {
                                ps.println4("var k = kv.Key;");
                            }

                            if (sr.refTable != null) {
                                ps.println4("var v = " + tableGet(sr.refTable, sr.refCols, "kv.Value"));
                                ps.println4("if (v == null) errors.RefNull(" + csv + ", ToString(), " + field + ", kv.Value);");
                            } else {
                                ps.println4("var v = kv.Value;");
                            }
                            ps.println4(refName(sr) + ".Add(k, v);");
                            ps.println3("}");
                        }
                    } else {
                        if (t instanceof TBeanRef && t.hasRef()) {
                            ps.println3(upper1(n) + "._resolve(errors);");
                        }

                        for (SRef sr : t.getConstraint().references) {
                            ps.println3(refName(sr) + " = " + tableGet(sr.refTable, sr.refCols, upper1(n)));
                            if (!sr.refNullable)
                                ps.println3("if (" + refName(sr) + " == null) errors.RefNull(" + csv + ", ToString(), " + field + ", " + upper1(n) + ");");
                        }
                    }
                }
            });


            tbean.getMRefs().forEach(m -> {
                ps.println3(refName(m) + " = " + tableGet(m.refTable, m.foreignKeyDefine.ref.cols, actualParams(m.foreignKeyDefine.keys)));
                if (m.foreignKeyDefine.refType != ForeignKey.RefType.NULLABLE)
                    ps.println3("if (" + refName(m) + " == null) errors.RefNull(" + csv + ", ToString(), \"" + m.name + "\", 0);");
            });

            tbean.getListRefs().forEach(l -> {
                ps.println3(refName(l) + " = new List<" + fullName(l.refTable) + ">();");
                ps.println3("foreach (var v in " + fullName(l.refTable) + ".All())");
                ps.println3("{");
                List<String> eqs = new ArrayList<>();
                for (int i = 0; i < l.foreignKeyDefine.keys.length; i++) {
                    String k = l.foreignKeyDefine.keys[i];
                    String rk = l.foreignKeyDefine.ref.cols[i];
                    eqs.add("v." + upper1(rk) + ".Equals(" + upper1(k) + ")");
                }
                ps.println3("if (" + String.join(" && ", eqs) + ")");
                ps.println4(refName(l) + ".Add(v);");
                ps.println3("}");
            });

            ps.println("	    }");
            ps.println();
        }

        ps.println("    }");
        ps.println("}");

    }

    private void generateMapGetBy(Map<String, Type> keys, GenCs.Name name, CachedIndentPrinter ps, boolean isPrimaryKey) {
        generateKeyClassIf(keys, ps);

        //static all
        String mapName = isPrimaryKey ? "all" : uniqueKeyMapName(keys);
        String allType = "Config.KeyedList<" + keyClassName(keys) + ", " + name.className + ">";
        ps.println2("static " + allType + " " + mapName + " = null;");
        ps.println();

        //static get
        String getByName = isPrimaryKey ? "Get" : uniqueKeyGetByName(keys);
        ps.println2("public static " + name.className + " " + getByName + "(" + formalParams(keys) + ")");
        ps.println2("{");
        ps.println3(name.className + " v;");
        ps.println3("return " + mapName + ".TryGetValue(" + actualParamsKey(keys) + ", out v) ? v : null;");
        ps.println2("}");
        ps.println();
    }

    private void generateAllMapPut(TTable ttable, CachedIndentPrinter ps) {
        generateMapPut(ttable.getPrimaryKey(), ps, true);
        for (Map<String, Type> uniqueKey : ttable.getUniqueKeys()) {
            generateMapPut(uniqueKey, ps, false);
        }
    }

    private void generateMapPut(Map<String, Type> keys, CachedIndentPrinter ps, boolean isPrimaryKey) {
        String mapName = isPrimaryKey ? "all" : uniqueKeyMapName(keys);
        ps.println4(mapName + ".Add(" + actualParamsKeySelf(keys) + ", self);");
    }


    private void generateKeyClassIf(Map<String, Type> keys, CachedIndentPrinter ps) {
        if (keys.size() > 1) {
            String keyClassName = keyClassName(keys);
            //static Key class
            ps.println2("class " + keyClassName);
            ps.println2("{");
            keys.forEach((n, t) -> ps.println3("readonly " + type(t) + " " + upper1(n) + ";"));
            ps.println();

            ps.println3("public " + keyClassName + "(" + formalParams(keys) + ")");
            ps.println3("{");
            keys.forEach((n, t) -> ps.println4("this." + upper1(n) + " = " + lower1(n) + ";"));
            ps.println3("}");
            ps.println();

            ps.println3("public override int GetHashCode()");
            ps.println3("{");
            ps.println4("return " + hashCodes(keys) + ";");
            ps.println3("}");

            ps.println3("public override bool Equals(object obj)");
            ps.println3("{");
            ps.println4("if (obj == null) return false;");
            ps.println4("if (obj == this) return true;");
            ps.println4("var o = obj as " + keyClassName + ";");
            ps.println4("return o != null && " + equals(keys) + ";");
            ps.println3("}");

            ps.println2("}");
            ps.println();
        }
    }

    private String uniqueKeyGetByName(Map<String, Type> keys) {
        return "GetBy" + keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b);
    }

    private String uniqueKeyMapName(Map<String, Type> keys) {
        return lower1(keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b) + "Map");
    }

    private String keyClassName(Map<String, Type> keys) {
        if (keys.size() > 1)
            return keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b) + "Key";
        else
            return type(keys.values().iterator().next());
    }

    private String formalParams(Map<String, Type> fs) {
        return fs.entrySet().stream().map(e -> type(e.getValue()) + " " + lower1(e.getKey())).collect(Collectors.joining(", "));
    }

    private String actualParams(String[] keys) {
        return Arrays.stream(keys).map(Generator::upper1).collect(Collectors.joining(", "));
    }

    private String actualParamsKey(Map<String, Type> keys) {
        String p = keys.keySet().stream().map(Generator::lower1).collect(Collectors.joining(", "));
        return keys.size() > 1 ? "new " + keyClassName(keys) + "(" + p + ")" : p;
    }

    private String actualParamsKeySelf(Map<String, Type> keys) {
        String p = keys.keySet().stream().map(n -> "self." + upper1(n)).collect(Collectors.joining(", "));
        return keys.size() > 1 ? "new " + keyClassName(keys) + "(" + p + ")" : p;
    }

    private String equals(Map<String, Type> fs) {
        return fs.keySet().stream().map(type -> upper1(type) + ".Equals(o." + upper1((type)) + ")").collect(Collectors.joining(" && "));
    }

    private String hashCodes(Map<String, Type> fs) {
        return fs.keySet().stream().map(type -> upper1(type) + ".GetHashCode()").collect(Collectors.joining(" + "));
    }

    private String toStrings(Map<String, Type> fs) {
        return fs.entrySet().stream().map(e -> toString(e.getKey(), e.getValue())).collect(Collectors.joining(" + \",\" + "));
    }

    private String toString(String n, Type t) {
        if (t instanceof TList)
            return "CSV.ToString(" + upper1(n) + ")";
        else
            return upper1(n);
    }

    private String tableGet(TTable ttable, String[] cols, String actualParam) {
        if (cols.length == 0) //ref to primary key
            return fullName(ttable) + ".Get(" + actualParam + ");";
        else
            return fullName(ttable) + ".GetBy" + Stream.of(cols).map(Generator::upper1).reduce("", (a, b) -> a + b) + "(" + actualParam + ");";
    }

    private String refType(Type t, SRef ref) {
        if (t instanceof TList) {
            return "List<" + fullName(ref.refTable) + ">";
        } else if (t instanceof TMap) {
            return "KeyedList<"
                    + (ref.mapKeyRefTable != null ? fullName(ref.mapKeyRefTable) : type(((TMap) t).key)) + ", "
                    + (ref.refTable != null ? fullName(ref.refTable) : type(((TMap) t).value)) + ">";
        } else {
            return fullName(ref.refTable);
        }
    }

    private String refName(SRef sr) {
        return (sr.refNullable ? "NullableRef" : "Ref") + upper1(sr.name);
    }

    private String refName(TForeignKey fk) {
        switch (fk.foreignKeyDefine.refType) {
            case NORMAL:
                return "Ref" + upper1(fk.name);
            case NULLABLE:
                return "NullableRef" + upper1(fk.name);
            default:
                return "ListRef" + upper1(fk.name);
        }
    }

    private String fullName(TBean tbean) {
        return new Name(pkg, prefix, tbean).fullName;
    }

    private String fullName(TTable cfg) {
        return fullName(cfg.getTBean());
    }

    private String type(Type t) {
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return "bool";
            }

            @Override
            public String visit(TInt type) {
                return "int";
            }

            @Override
            public String visit(TLong type) {
                return "long";
            }

            @Override
            public String visit(TFloat type) {
                return "float";
            }

            @Override
            public String visit(TString type) {
                return "string";
            }

            @Override
            public String visit(TList type) {
                return "List<" + type(type.value) + ">";
            }

            @Override
            public String visit(TMap type) {
                return "KeyedList<" + type(type.key) + ", " + type(type.value) + ">";
            }

            @Override
            public String visit(TBean type) {
                return fullName(type);
            }

            @Override
            public String visit(TBeanRef type) {
                return fullName(type.tBean);
            }
        });
    }

    private String _create(Type t) {
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return "os.ReadBool()";
            }

            @Override
            public String visit(TInt type) {
                return "os.ReadInt32()";
            }

            @Override
            public String visit(TLong type) {
                return "os.ReadInt64()";
            }

            @Override
            public String visit(TFloat type) {
                return "os.ReadSingle()";
            }

            @Override
            public String visit(TString type) {
                return "os.ReadString()";
            }

            @Override
            public String visit(TList type) {
                return null;
            }

            @Override
            public String visit(TMap type) {
                return null;
            }

            @Override
            public String visit(TBean type) {
                return fullName(type) + "._create(os)";
            }

            @Override
            public String visit(TBeanRef type) {
                return fullName(type.tBean) + "._create(os)";
            }
        });
    }

    private void copyFile(String file) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/support/" + file);
             BufferedReader br = new BufferedReader(new InputStreamReader(is != null ? is : new FileInputStream("src/support/" + file), "GBK"));
             CachedIndentPrinter ps = createCode(new File(dstDir, file), encoding)) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                ps.println(line);
            }
        }
    }

    private void genCSVProcessor() throws IOException {
        try (CachedIndentPrinter ps = createCode(new File(dstDir, "CSVProcessor.cs"), encoding)) {
            ps.println("using System.Collections.Generic;");
            if (!pkg.equals("Config")) {
                ps.println("using Config;");
            }
            ps.println();
            ps.println("namespace " + pkg);
            ps.println("{");

            ps.println1("public static class CSVProcessor");
            ps.println1("{");
            ps.println2("public static readonly LoadErrors Errors = new LoadErrors();");
            ps.println();
            ps.println2("public static void Process(Config.Stream os)");
            ps.println2("{");
            ps.println3("var configNulls = new List<string>");
            ps.println3("{");
            for (TTable cfg : value.getTDb().getTTables()) {
                ps.println4("\"" + cfg.name + "\",");
            }
            ps.println3("};");

            ps.println3("for(;;)");
            ps.println3("{");
            ps.println4("var csv = os.ReadCfg();");
            ps.println4("if (csv == null)");
            ps.println5("break;");

            ps.println4("switch(csv)");
            ps.println4("{");
            for (TTable cfg : value.getTDb().getTTables()) {
                ps.println5("case \"" + cfg.name + "\":");
                ps.println6("configNulls.Remove(csv);");
                ps.println6(fullName(cfg.getTBean()) + ".Initialize(os, Errors);");
                ps.println6("break;");
            }
            ps.println5("default:");
            ps.println6("Errors.ConfigDataAdd(csv);");
            ps.println6("break;");
            ps.println4("}");
            ps.println3("}");

            ps.println3("foreach (var csv in configNulls)");
            ps.println4("Errors.ConfigNull(csv);");

            for (TTable c : value.getTDb().getTTables()) {
                if (c.getTBean().hasRef()) {
                    ps.println3(fullName(c) + ".Resolve(Errors);");
                }
            }

            ps.println2("}");
            ps.println();
            ps.println1("}");
            ps.println("}");
            ps.println();
        }
    }

}
