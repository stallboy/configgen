package configgen.genjava;

import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.ForeignKey;
import configgen.define.Table;
import configgen.gen.*;
import configgen.type.*;
import configgen.util.CachedFiles;
import configgen.util.CachedIndentPrinter;
import configgen.value.VDb;
import configgen.value.VTable;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenJavaCode extends Generator {

    public static void register() {
        Generators.addProvider("java", new GeneratorProvider() {
            @Override
            public Generator create(Parameter parameter) {
                return new GenJavaCode(parameter);
            }

            @Override
            public String usage() {
                return "dir:config,pkg:config,encoding:UTF-8    cooperate with -gen zip";
            }
        });
    }

    private VDb value;
    private File dstDir;
    private final String dir;
    private final String pkg;
    private final String encoding;

    GenJavaCode(Parameter parameter) {
        super(parameter);
        dir = parameter.get("dir", "config");
        pkg = parameter.getNotEmpty("pkg", "config");
        encoding = parameter.get("encoding", "UTF-8");

        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        value = ctx.makeValue();
        dstDir = Paths.get(dir).resolve(pkg.replace('.', '/')).toFile();


        for (TBean tbean : value.getDbType().tbeans.values()) {
            generateBeanClass(tbean);
            for (TBean actionBean : tbean.actionBeans.values()) {
                generateBeanClass(actionBean);
            }
        }

        File mgrFile = dstDir.toPath().resolve("ConfigMgr.java").toFile();
        try (CachedIndentPrinter mgrPrint = createCode(mgrFile, encoding)) {

            mgrPrint.println("package %s;", pkg);
            mgrPrint.println();

            mgrPrint.println("public class ConfigMgr {");

            mgrPrint.println1("private static volatile ConfigMgr mgr;");
            mgrPrint.println();
            mgrPrint.println1("public static ConfigMgr getMgr(){");
            mgrPrint.println2("return mgr;");
            mgrPrint.println1("}");
            mgrPrint.println();

            mgrPrint.println1("public static void setMgr(ConfigMgr newMgr){");
            mgrPrint.println2("mgr = newMgr;");
            mgrPrint.println1("}");
            mgrPrint.println();


            for (VTable vtable : value.getVTables()) {
                generateTableClass(vtable, mgrPrint);
                mgrPrint.println();
            }

            mgrPrint.println("}");
        }


        genConfigMgrLoader(value);
        genConfigSchema(value);
        CachedFiles.deleteOtherFiles(dstDir);
    }

    private class Name {
        final String pkg;
        final String className;
        final String fullName;
        final String path;
        final String containerPrefix;

        Name(TBean tbean) {
            this(tbean, "");
        }

        Name(TBean tbean, String postfix) {
            String topPkg = GenJavaCode.this.pkg;
            String name;
            if (tbean.beanDefine.type == Bean.BeanType.Action) {
                TBean baseAction = (TBean) tbean.parent;
                name = baseAction.name.toLowerCase() + "." + tbean.name;
            } else {
                name = tbean.name;
            }

            name += postfix;
            containerPrefix = tbean.name.replace('.', '_') + "_";
            String[] seps = name.split("\\.");
            String c = seps[seps.length - 1];
            className = c.substring(0, 1).toUpperCase() + c.substring(1);

            String[] pks = Arrays.copyOf(seps, seps.length - 1);
            if (pks.length == 0)
                pkg = topPkg;
            else
                pkg = topPkg + "." + String.join(".", pks);

            fullName = pkg + "." + className;
            if (pks.length == 0)
                path = className + ".java";
            else
                path = String.join("/", pks) + "/" + className + ".java";

        }
    }

    private void generateBeanClass(TBean tbean) throws IOException {
        Name name = new Name(tbean);
        File javaFile = dstDir.toPath().resolve(name.path).toFile();

        try (CachedIndentPrinter ps = createCode(javaFile, encoding)) {
            if (tbean.beanDefine.type == Bean.BeanType.BaseAction) {
                generateBaseActionClass(tbean, name, ps);
            } else {
                generateBeanClass(tbean, null, name, ps, null);
            }
        }
    }

    private void generateTableClass(VTable vtable, CachedIndentPrinter mgrPrint) throws IOException {
        boolean isNeedReadData = true;
        String dataPostfix = "";
        Table define = vtable.tableType.tableDefine;
        if (define.isEnum()) {
            String entryPostfix = "";
            if (define.isEnumFull()) {
                if (define.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                    isNeedReadData = false;
                } else {
                    dataPostfix = "_Detail";
                }
            } else {
                entryPostfix = "_Entry";
            }

            Name name = new Name(vtable.tableType.tbean, entryPostfix);
            Name dataName = new Name(vtable.tableType.tbean, dataPostfix);
            File javaFile = dstDir.toPath().resolve(name.path).toFile();
            try (CachedIndentPrinter ps = createCode(javaFile, encoding)) {
                generateEnumClass(vtable, name, ps, define.isEnumFull(), isNeedReadData, dataName);
            }
        }

        if (isNeedReadData) {
            Name name = new Name(vtable.tableType.tbean, dataPostfix);
            File javaFile = dstDir.toPath().resolve(name.path).toFile();

            try (CachedIndentPrinter ps = createCode(javaFile, encoding)) {
                generateBeanClass(vtable.tableType.tbean, vtable, name, ps, mgrPrint);
            }
        }
    }

    private void generateBaseActionClass(TBean tbean, Name name, CachedIndentPrinter ps) {
        ps.println("package %s;", name.pkg);
        ps.println();
        ps.println("public interface %s {", name.className);
        ps.inc();
        ps.println("%s type();", fullName(tbean.actionEnumRefTable));
        ps.println();

        if (tbean.hasRef()) {
            ps.println("default void _resolve(%s.ConfigMgr mgr) {", pkg);
            ps.println("}");
            ps.println();
        }

        ps.println("static %s _create(configgen.genjava.ConfigInput input) {", name.className);
        ps.inc();
        ps.println("switch(input.readStr()) {");
        for (TBean actionBean : tbean.actionBeans.values()) {
            ps.println1("case \"%s\":", actionBean.name);
            ps.println2("return %s._create(input);", fullName(actionBean));
        }

        ps.println("}");
        ps.println("throw new IllegalArgumentException();");
        ps.dec();
        ps.println("}");
        ps.dec();
        ps.println("}");
    }

    private void generateEnumClass(VTable vtable, Name name, CachedIndentPrinter ps, boolean isFull, boolean isNeedReadData, Name dataName) throws IOException {
        ps.println("package " + name.pkg + ";");
        ps.println();

        ps.println((isFull ? "public enum " : "public class ") + name.className + " {");

        boolean hasIntValue = !vtable.tableType.tableDefine.isEnumAsPrimaryKey();


        if (!hasIntValue) {
            int len = vtable.enumNames.size();
            int c = 0;
            for (String enumName : vtable.enumNames) {
                c++;
                String fix = c == len ? ";" : ",";
                if (isFull) {
                    ps.println1("%s(\"%s\")%s", enumName.toUpperCase(), enumName, fix);
                } else {
                    ps.println1("public static %s %s = new %s(\"%s\");", name.className, enumName.toUpperCase(), name.className, enumName);
                }
            }

            ps.println();
            ps.println1("private String value;");
            ps.println();

            ps.println1("%s(String value) {", name.className);
            ps.println2("this.value = value;");
            ps.println1("}");
            ps.println();

//            ps.println1("public String getValue() {");
//            ps.println2("return value;");
//            ps.println1("}");
//            ps.println();

        } else {
            int len = vtable.enumName2IntegerValueMap.size();
            int c = 0;
            for (Map.Entry<String, Integer> entry : vtable.enumName2IntegerValueMap.entrySet()) {
                String enumName = entry.getKey();
                int value = entry.getValue();
                c++;
                String fix = c == len ? ";" : ",";

                if (isFull) {
                    ps.println1("%s(\"%s\", %d)%s", enumName.toUpperCase(), enumName, value, fix);
                } else {
                    ps.println1("public static %s %s = new %s(\"%s\", %d);", name.className, enumName.toUpperCase(), name.className, enumName, value);
                }
            }

            ps.println();
            ps.println1("private String name;");
            ps.println1("private int value;");

            ps.println();

            ps.println1("%s(String name, int value) {", name.className);
            ps.println2("this.name = name;");
            ps.println2("this.value = value;");
            ps.println1("}");
            ps.println();

//            ps.println1("public String getName() {");
//            ps.println2("return name;");
//            ps.println1("}");
//            ps.println();
//
//            ps.println1("public int getValue() {");
//            ps.println2("return value;");
//            ps.println1("}");
//            ps.println();
        }


        if (isFull) {
            ps.println1("private static java.util.Map<%s, %s> map = new java.util.HashMap<>();", hasIntValue ? "Integer" : "String", name.className);
            ps.println();

            ps.println1("static {");
            ps.println2("for(%s e : %s.values()) {", name.className, name.className);
            ps.println3("map.put(e.value, e);");
            ps.println2("}");
            ps.println1("}");
            ps.println();

            ps.println1("public static %s get(%s value) {", name.className, hasIntValue ? "int" : "String");
            ps.println2("return map.get(value);");
            ps.println1("}");
            ps.println();

            TBean tbean = vtable.tableType.tbean;
            tbean.columns.forEach((n, t) -> {
                Column f = tbean.beanDefine.columns.get(n);
                if (!f.desc.isEmpty()) {
                    ps.println1("/**");
                    ps.println1(" * " + f.desc);
                    ps.println1(" */");
                }
                ps.println1("public " + type(t) + " get" + upper1(n) + "() {");
                if (f.name.equals(vtable.tableType.tableDefine.primaryKey[0])) {
                    ps.println2("return value;");
                } else if (f.name.equals(vtable.tableType.tableDefine.enumStr)) {
                    ps.println2("return name;");
                } else {
                    ps.println2("return ref().get" + upper1(n) + "();");
                }
                ps.println1("}");
                ps.println();

                t.constraint.references.forEach(r -> {
                    ps.println1("public " + refType(t, r) + " " + lower1(refName(r)) + "() {");
                    ps.println2("return ref()." + lower1(refName(r)) + "();");
                    ps.println1("}");
                    ps.println();
                });
            });

            tbean.mRefs.forEach(m -> {
                ps.println1("public " + fullName(m.refTable) + " " + lower1(refName(m)) + "() {");
                ps.println2("return ref()." + lower1(refName(m)) + "();");
                ps.println1("}");
                ps.println();
            });

            tbean.listRefs.forEach(l -> {
                ps.println1("public " + listRefFullName(tbean, l) + " " + lower1(refName(l)) + "() {");
                ps.println2("return ref()." + lower1(refName(l)) + "();");
                ps.println1("}");
                ps.println();
            });
        }

        if (isNeedReadData) {
            ps.println1("public %s ref() {", dataName.fullName);
            ps.println2("return %s.get(value);", dataName.fullName);
            ps.println1("}");
            ps.println();
        }

        ps.println("}");
    }

    private void generateBeanClass(TBean tbean, VTable vtable, Name name, CachedIndentPrinter ps, CachedIndentPrinter mgrPrint) {
        boolean isBean = vtable == null;
        boolean isTable = !isBean;
        TTable ttable = isTable ? vtable.tableType : null;

        ps.println("package " + name.pkg + ";");
        ps.println();
        if (tbean.beanDefine.type == Bean.BeanType.Action) {
            TBean baseAction = (TBean) tbean.parent;
            ps.println("public class " + name.className + " implements " + fullName(baseAction) + " {");
            ps.println1("@Override");
            ps.println1("public " + fullName(baseAction.actionEnumRefTable) + " type() {");
            ps.println2("return " + fullName(baseAction.actionEnumRefTable) + "." + tbean.name.toUpperCase() + ";");
            ps.println1("}");
            ps.println();
        } else {
            ps.println("public class " + name.className + " {");
        }

        //field
        tbean.columns.forEach((n, t) -> {
            ps.println1("private " + type(t) + " " + lower1(n) + initialValue(t) + ";");
            t.constraint.references.forEach(r -> ps.println1("private " + refType(t, r) + " " + refName(r) + refInitialValue(t) + ";"));
        });

        tbean.mRefs.forEach(m -> ps.println1("private " + fullName(m.refTable) + " " + refName(m) + ";"));
        tbean.listRefs.forEach(l -> ps.println1("private " + listRefFullName(tbean, l) + " " + refName(l) + " = new java.util.ArrayList<>();"));
        ps.println();

        //constructor
        ps.println1("private %s() {", name.className);
        ps.println1("}");
        ps.println();

        if (isBean) {
            ps.println1("public %s(%s) {", name.className, formalParams(tbean.columns));
            tbean.columns.forEach((n, t) -> ps.println2("this.%s = %s;", lower1(n), lower1(n)));
            ps.println1("}");
            ps.println();
        }

        //static create from ConfigInput
        ps.println1("public static %s _create(configgen.genjava.ConfigInput input) {", name.className, pkg);
        ps.println2("%s self = new %s();", name.className, name.className);

        for (Map.Entry<String, Type> f : tbean.columns.entrySet()) {
            String n = f.getKey();
            Type t = f.getValue();
            String selfN = "self." + lower1(n);
            if (t instanceof TList) {
                ps.println2("for (int c = input.readInt(); c > 0; c--) {");
                ps.println3("%s.add(%s);", selfN, _create(((TList) t).value));
                ps.println2("}");
            } else if (t instanceof TMap) {
                ps.println2("for (int c = input.readInt(); c > 0; c--) {");
                ps.println3("%s.put(%s, %s);", selfN, _create(((TMap) t).key), _create(((TMap) t).value));
                ps.println2("}");
            } else {
                ps.println2("%s = %s;", selfN, _create(t));
            }
        }
        ps.println2("return self;");
        ps.println1("}");
        ps.println();


        //getter
        tbean.columns.forEach((n, t) -> {
            Column f = tbean.beanDefine.columns.get(n);
            if (!f.desc.isEmpty()) {
                ps.println1("/**");
                ps.println1(" * " + f.desc);
                ps.println1(" */");
            }

            ps.println1("public " + type(t) + " get" + upper1(n) + "() {");
            ps.println2("return " + lower1(n) + ";");
            ps.println1("}");
            ps.println();

            t.constraint.references.forEach(r -> {
                ps.println1("public " + refType(t, r) + " " + lower1(refName(r)) + "() {");
                ps.println2("return " + refName(r) + ";");
                ps.println1("}");
                ps.println();
            });
        });

        tbean.mRefs.forEach(m -> {
            ps.println1("public " + fullName(m.refTable) + " " + lower1(refName(m)) + "() {");
            ps.println2("return " + refName(m) + ";");
            ps.println1("}");
            ps.println();
        });

        tbean.listRefs.forEach(l -> {
            ps.println1("public " + listRefFullName(tbean, l) + " " + lower1(refName(l)) + "() {");
            ps.println2("return " + refName(l) + ";");
            ps.println1("}");
            ps.println();
        });

        //hashCode, equals
        if (isBean) {
            Map<String, Type> keys = tbean.columns;
            ps.println1("@Override");
            ps.println1("public int hashCode() {");
            ps.println2("return " + hashCodes(keys) + ";");
            ps.println1("}");
            ps.println();

            ps.println1("@Override");
            ps.println1("public boolean equals(Object other) {");
            ps.println2("if (null == other || !(other instanceof " + name.className + "))");
            ps.println3("return false;");
            ps.println2(name.className + " o = (" + name.className + ") other;");
            ps.println2("return " + equals(keys) + ";");
            ps.println1("}");
            ps.println();
        }

        //toString
        ps.println1("@Override");
        ps.println1("public String toString() {");
        ps.println2("return \"(\" + " + String.join(" + \",\" + ", tbean.columns.keySet().stream().map(Generator::lower1).collect(Collectors.toList())) + " + \")\";");
        ps.println1("}");
        ps.println();


        //_resolve
        if (tbean.hasRef()) {
            if (tbean.beanDefine.type == Bean.BeanType.Action) {
                ps.println1("@Override");
            }
            ps.println1("public void _resolve(%s.ConfigMgr mgr) {", pkg);

            for (Map.Entry<String, Type> f : tbean.columns.entrySet()) {
                String n = f.getKey();
                Type t = f.getValue();
                if (t.hasRef()) {
                    if (t instanceof TList) {
                        TList tt = (TList) t;
                        ps.println2(lower1(n) + ".forEach( e -> {");
                        if (tt.value instanceof TBean && tt.value.hasRef()) {
                            ps.println3("e._resolve(mgr);");
                        }
                        for (SRef sr : t.constraint.references) {
                            ps.println3(fullName(sr.refTable) + " r = " + tableGet(sr.refTable, sr.refCols, "e"));
                            ps.println3("java.util.Objects.requireNonNull(r);");
                            ps.println3(refName(sr) + ".add(r);");
                        }
                        ps.println2("});");
                    } else if (t instanceof TMap) {
                        TMap tt = (TMap) t;
                        ps.println2(lower1(n) + ".forEach( (k, v) -> {");
                        if (tt.key instanceof TBean && tt.key.hasRef()) {
                            ps.println3("k._resolve(mgr);");
                        }
                        if (tt.value instanceof TBean && tt.value.hasRef()) {
                            ps.println3("v._resolve(mgr);");
                        }
                        for (SRef sr : t.constraint.references) {
                            String k = "k";
                            if (sr.mapKeyRefTable != null) {
                                ps.println3(fullName(sr.mapKeyRefTable) + " rk = " + tableGet(sr.mapKeyRefTable, sr.mapKeyRefCols, "k"));
                                ps.println3("java.util.Objects.requireNonNull(rk);");
                                k = "rk";
                            }
                            String v = "v";
                            if (sr.refTable != null) {
                                ps.println3(fullName(sr.refTable) + " rv = " + tableGet(sr.refTable, sr.refCols, "v"));
                                ps.println3("java.util.Objects.requireNonNull(rv);");
                                v = "rv";
                            }
                            ps.println3(refName(sr) + ".put(" + k + ", " + v + ");");
                        }
                        ps.println2("});");
                    } else {
                        if (t instanceof TBean && t.hasRef()) {
                            ps.println2(lower1(n) + "._resolve(mgr);");
                        }
                        for (SRef sr : t.constraint.references) {
                            ps.println2(refName(sr) + " = " + tableGet(sr.refTable, sr.refCols, lower1(n)));
                            if (!sr.refNullable)
                                ps.println2("java.util.Objects.requireNonNull(" + refName(sr) + ");");
                        }
                    }
                }
            } //end columns

            tbean.mRefs.forEach(m -> {
                ps.println2(refName(m) + " = " + tableGet(m.refTable, m.foreignKeyDefine.ref.cols, actualParams(m.foreignKeyDefine.keys)));
                if (m.foreignKeyDefine.refType != ForeignKey.RefType.NULLABLE)
                    ps.println2("java.util.Objects.requireNonNull(" + refName(m) + ");");
            });

            tbean.listRefs.forEach(l -> {
                boolean gen = false;
                if (l.foreignKeyDefine.keys.length == 1) {
                    String k = l.foreignKeyDefine.keys[0];
                    String rk = l.foreignKeyDefine.ref.cols[0];
                    Type col = tbean.columns.get(k);
                    if (col instanceof TList) {
                        gen = true;
                    } else if (col instanceof TMap) {
                        //TODO
                        gen = true;
                    }
                }

                if (!gen) {
                    Name refn = new Name(l.refTable.tbean);
                    ps.println2("mgr." + refn.containerPrefix + "All.values().forEach( v -> {");
                    List<String> eqs = new ArrayList<>();
                    for (int i = 0; i < l.foreignKeyDefine.keys.length; i++) {
                        String k = l.foreignKeyDefine.keys[i];
                        String rk = l.foreignKeyDefine.ref.cols[i];
                        eqs.add(equal("v.get" + upper1(rk) + "()", lower1(k), tbean.columns.get(k)));
                    }
                    ps.println3("if (" + String.join(" && ", eqs) + ")");
                    ps.println4(refName(l) + ".add(v);");
                    ps.println2("});");
                }
            });

            ps.println1("}");
            ps.println();
        } //end _resolve


        if (isTable) {
            //static get
            generateMapGetBy(ttable.primaryKey, name, ps, true, mgrPrint);

            //static getByXxx
            for (Map<String, Type> uniqueKey : ttable.uniqueKeys) {
                generateMapGetBy(uniqueKey, name, ps, false, mgrPrint);
            }

            //static all
            ps.println1("public static java.util.Collection<" + name.className + "> all() {");
            ps.println2("%s.ConfigMgr mgr = %s.ConfigMgr.getMgr();", pkg, pkg);
            ps.println2("return mgr.%sAll.values();", name.containerPrefix);
            ps.println1("}");
            ps.println();

            //static _createAll
            ps.println1("public static void _createAll(%s.ConfigMgr mgr, configgen.genjava.ConfigInput input) {", pkg);
            ps.println2("for (int c = input.readInt(); c > 0; c--) {");
            ps.println3("%s self = %s._create(input);", name.className, name.className);
            generateAllMapPut(ttable, name, ps);
            ps.println2("}");
            ps.println1("}");
            ps.println();

            //static _resolveAll
            if (tbean.hasRef()) {
                ps.println1("public static void _resolveAll(%s.ConfigMgr mgr) {", pkg);
                ps.println2("for (%s e : mgr.%sAll.values()) {", name.className, name.containerPrefix);
                ps.println3("e._resolve(mgr);");
                ps.println2("}");
                ps.println1("}");
                ps.println();
            }
        } //end cfg != null
        ps.println("}");
    }

    private String _create(Type t) {
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return "input.readBool()";
            }

            @Override
            public String visit(TInt type) {
                return "input.readInt()";
            }

            @Override
            public String visit(TLong type) {
                return "input.readLong()";
            }

            @Override
            public String visit(TFloat type) {
                return "input.readFloat()";
            }

            @Override
            public String visit(TString type) {
                return "input.readStr()";
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
                return fullName(type) + "._create(input)";
            }
        });

    }

    private void generateMapGetBy(Map<String, Type> keys, Name name, CachedIndentPrinter ps, boolean isPrimaryKey, CachedIndentPrinter mgrPrint) {
        if (keys.size() > 1) {
            generateKeyClass(keys, ps);
        }

        String mapName = name.containerPrefix + (isPrimaryKey ? "All" : uniqueKeyMapName(keys));
        String keyName = keyClassName(keys);
        if (keys.size() > 1) {
            keyName = name.fullName + "." + keyName;
        }
        mgrPrint.println1("public final java.util.Map<%s, %s> %s = new java.util.LinkedHashMap<>();", keyName, name.fullName, mapName);

        String getByName = isPrimaryKey ? "get" : uniqueKeyGetByName(keys);
        ps.println1("public static " + name.className + " " + getByName + "(" + formalParams(keys) + ") {");
        ps.println2("%s.ConfigMgr mgr = %s.ConfigMgr.getMgr();", pkg, pkg);
        ps.println2("return mgr." + mapName + ".get(" + actualParamsKey(keys, "") + ");");
        ps.println1("}");
        ps.println();
    }


    private String tableGet(TTable ttable, String[] cols, String actualParam) {
        boolean isPrimaryKey = cols.length == 0;
        Name name = new Name(ttable.tbean);

        if (ttable.tableDefine.isEnumFull()) {
            return name.fullName + ".get(" + actualParam + ");";
        } else {
            String pre = "mgr." + name.containerPrefix;

            if (isPrimaryKey) {//ref to primary key
                if (ttable.primaryKey.size() == 1) {
                    return pre + "All.get(" + actualParam + ");";
                } else {
                    return pre + "All.get(new " + name.fullName + "." + multiKeyClassName(ttable.tableDefine.primaryKey) + "(" + actualParam + ") );";
                }
            } else {
                if (cols.length == 1) {
                    return pre + uniqueKeyMapName(cols) + ".get(" + actualParam + ");";
                } else {
                    return pre + uniqueKeyMapName(cols) + ".get( new " + name.fullName + "." + multiKeyClassName(cols) + "(" + actualParam + ") );";
                }
            }
        }
    }

    private void generateAllMapPut(TTable ttable, Name name, CachedIndentPrinter ps) {
        generateMapPut(ttable.primaryKey, name, ps, true);
        for (Map<String, Type> uniqueKey : ttable.uniqueKeys) {
            generateMapPut(uniqueKey, name, ps, false);
        }
    }

    private void generateMapPut(Map<String, Type> keys, Name name, CachedIndentPrinter ps, boolean isPrimaryKey) {
        String mapName = name.containerPrefix + (isPrimaryKey ? "All" : uniqueKeyMapName(keys));
        ps.println3("mgr." + mapName + ".put(" + actualParamsKey(keys, "self.") + ", self);");
    }

    private void generateKeyClass(Map<String, Type> keys, CachedIndentPrinter ps) {
        String keyClassName = keyClassName(keys);
        //static Key class
        ps.println1("public static class " + keyClassName + " {");
        keys.forEach((n, t) -> ps.println2("private " + type(t) + " " + lower1(n) + ";"));
        ps.println();

        ps.println2(keyClassName + "(" + formalParams(keys) + ") {");
        keys.forEach((n, t) -> ps.println3("this." + lower1(n) + " = " + lower1(n) + ";"));
        ps.println2("}");
        ps.println();

        ps.println2("@Override");
        ps.println2("public int hashCode() {");
        ps.println3("return " + hashCodes(keys) + ";");
        ps.println2("}");
        ps.println();

        ps.println2("@Override");
        ps.println2("public boolean equals(Object other) {");
        ps.println3("if (null == other || !(other instanceof " + keyClassName + "))");
        ps.println4("return false;");
        ps.println3(keyClassName + " o = (" + keyClassName + ") other;");
        ps.println3("return " + equals(keys) + ";");
        ps.println2("}");

        ps.println1("}");
        ps.println();
    }

    private String uniqueKeyGetByName(Map<String, Type> keys) {
        return "getBy" + keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b);
    }

    private String uniqueKeyMapName(Map<String, Type> keys) {
        return keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b) + "Map";
    }

    private String uniqueKeyMapName(String[] keys) {
        return Stream.of(keys).map(Generator::upper1).reduce("", (a, b) -> a + b) + "Map";
    }


    private String keyClassName(Map<String, Type> keys) {
        if (keys.size() > 1)
            return keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b) + "Key";
        else
            return boxType(keys.values().iterator().next());
    }

    private String multiKeyClassName(String[] keys) {
        return Stream.of(keys).map(Generator::upper1).reduce("", (a, b) -> a + b) + "Key";
    }


    private String type(Type t) {
        return _type(t, false);
    }

    private String boxType(Type t) {
        return _type(t, true);
    }

    private String _type(Type t, boolean box) {
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return box ? "Boolean" : "boolean";
            }

            @Override
            public String visit(TInt type) {
                return box ? "Integer" : "int";
            }

            @Override
            public String visit(TLong type) {
                return box ? "Long" : "long";
            }

            @Override
            public String visit(TFloat type) {
                return box ? "Float" : "float";
            }

            @Override
            public String visit(TString type) {
                return "String";
            }

            @Override
            public String visit(TList type) {
                return "java.util.List<" + _type(type.value, true) + ">";
            }

            @Override
            public String visit(TMap type) {
                return "java.util.Map<" + _type(type.key, true) + ", " + _type(type.value, true) + ">";
            }

            @Override
            public String visit(TBean type) {
                return fullName(type);
            }
        });
    }


    private String initialValue(Type t) {
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return "";
            }

            @Override
            public String visit(TInt type) {
                return "";
            }

            @Override
            public String visit(TLong type) {
                return "";
            }

            @Override
            public String visit(TFloat type) {
                return "";
            }

            @Override
            public String visit(TString type) {
                return "";
            }

            @Override
            public String visit(TList type) {
                return " = new java.util.ArrayList<>()";
            }

            @Override
            public String visit(TMap type) {
                return " = new java.util.LinkedHashMap<>()";
            }

            @Override
            public String visit(TBean type) {
                return "";
            }
        });
    }

    private String listRefFullName(TBean tbean, TForeignKey tfk) {
        return "java.util.List<" + fullName(tbean, tfk) + ">";
    }

    private String fullName(TBean tbean, TForeignKey tfk) {
        String name = fullName(tfk.refTable);
        if (tfk.foreignKeyDefine.keys.length == 1) {
            String k = tfk.foreignKeyDefine.keys[0];
            Type tt = tbean.columns.get(k);
            if (tt instanceof TList) {
                return "java.util.List<" + name + ">";
            } else if (tt instanceof TMap) {
                //TODO
                return "";
            }
        }
        return name;
    }

    private String fullName(TBean tbean) {
        return new Name(tbean).fullName;
    }

    private String fullName(TTable ttable) {
        return fullName(ttable.tbean);
    }

    private String tableDataFullName(TTable ttable) {
        String fn = fullName(ttable.tbean);
        if (ttable.tableDefine.isEnumFull() && !ttable.tableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
            fn = fn + "_Detail";
        }
        return fn;
    }


    private String refType(Type t, SRef ref) {
        if (t instanceof TList) {
            return "java.util.List<" + fullName(ref.refTable) + ">";
        } else if (t instanceof TMap) {
            return "java.util.Map<"
                    + (ref.mapKeyRefTable != null ? fullName(ref.mapKeyRefTable) : boxType(((TMap) t).key)) + ", "
                    + (ref.refTable != null ? fullName(ref.refTable) : boxType(((TMap) t).value)) + ">";
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

    private String refInitialValue(Type t) {
        if (t instanceof TList) {
            return " = new java.util.ArrayList<>()";
        } else if (t instanceof TMap) {
            return " = new java.util.LinkedHashMap<>();";
        } else {
            return "";
        }
    }

    private String formalParams(Map<String, Type> fs) {
        return String.join(", ", fs.entrySet().stream().map(e -> type(e.getValue()) + " " + lower1(e.getKey())).collect(Collectors.toList()));
    }

    private String actualParams(String[] keys) {
        return String.join(", ", Arrays.asList(keys).stream().map(Generator::lower1).collect(Collectors.toList()));
    }


    private String actualParamsKey(Map<String, Type> keys, String pre) {
        String p = String.join(", ", keys.entrySet().stream().map(e -> pre + lower1(e.getKey())).collect(Collectors.toList()));
        return keys.size() > 1 ? "new " + keyClassName(keys) + "(" + p + ")" : p;
    }

    private String hashCodes(Map<String, Type> fs) {
        return String.join(" + ", fs.entrySet().stream().map(e -> hashCode(e.getKey(), e.getValue())).collect(Collectors.toList()));
    }

    private static String hashCode(String name, Type t) {
        String n = lower1(name);
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return "Boolean.hashCode(" + n + ")";
            }

            @Override
            public String visit(TInt type) {
                return n;
            }

            @Override
            public String visit(TLong type) {
                return "Long.hashCode(" + n + ")";
            }

            @Override
            public String visit(TFloat type) {
                return "Float.hashCode(" + n + ")";
            }

            @Override
            public String visit(TString type) {
                return n + ".hashCode()";
            }

            @Override
            public String visit(TList type) {
                return n + ".hashCode()";
            }

            @Override
            public String visit(TMap type) {
                return n + ".hashCode()";
            }

            @Override
            public String visit(TBean type) {
                return n + ".hashCode()";
            }
        });
    }

    private String equals(Map<String, Type> fs) {
        return String.join(" && ", fs.entrySet().stream().map(e -> equal(lower1(e.getKey()), "o." + lower1(e.getKey()), e.getValue())).collect(Collectors.toList()));
    }

    private String equal(String a, String b, Type t) {
        boolean eq = t.accept(new TypeVisitorT<Boolean>() {
            @Override
            public Boolean visit(TBool type) {
                return false;
            }

            @Override
            public Boolean visit(TInt type) {
                return false;
            }

            @Override
            public Boolean visit(TLong type) {
                return false;
            }

            @Override
            public Boolean visit(TFloat type) {
                return false;
            }

            @Override
            public Boolean visit(TString type) {
                return true;
            }

            @Override
            public Boolean visit(TList type) {
                return true;
            }

            @Override
            public Boolean visit(TMap type) {
                return true;
            }

            @Override
            public Boolean visit(TBean type) {
                return true;
            }
        });
        return eq ? a + ".equals(" + b + ")" : a + " == " + b;
    }


    private void genConfigMgrLoader(VDb vdb) throws IOException {
        try (CachedIndentPrinter ps = createCode(new File(dstDir, "ConfigMgrLoader.java"), encoding)) {
            ps.println("package " + pkg + ";");
            ps.println();

            ps.println("public class ConfigMgrLoader {");
            ps.println();

            ps.println1("public static ConfigMgr load(configgen.genjava.ConfigInput input) {");
            ps.println2("ConfigMgr mgr = new ConfigMgr();");

            int cnt = 0;
            for (VTable vTable : vdb.getVTables()) {
                if (vTable.tableType.tableDefine.isEnumFull() && vTable.tableType.tableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                    continue;
                }
                cnt++;
            }


            ps.println2("int c = input.readInt();");
            ps.println2("if (c < %d) {", cnt);
            ps.println3("throw new IllegalArgumentException();");
            ps.println2("}");
            ps.println2("for (int i = 0; i < c; i++) {");
            ps.println3("String tableName = input.readStr();");
            ps.println3("int tableSize = input.readInt();");
            ps.println3("switch (tableName) {");
            for (VTable vTable : vdb.getVTables()) {
                if (vTable.tableType.tableDefine.isEnumFull() && vTable.tableType.tableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                    continue;
                }

                Name name = new Name(vTable.tableType.tbean);

                ps.println4("case \"%s\":", vTable.name);
                ps.println5("%s._createAll(mgr, input);", tableDataFullName(vTable.tableType));
                ps.println5("break;");
            }

            ps.println4("default:");
            ps.println5("input.skipBytes(tableSize);");
            ps.println5("break;");
            ps.println3("}");
            ps.println2("}");
            ps.println();

            for (VTable vTable : vdb.getVTables()) {
                if (vTable.tableType.tableDefine.isEnumFull() && vTable.tableType.tableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                    continue;
                }

                if (vTable.tableType.tbean.hasRef()) {
                    ps.println2("%s._resolveAll(mgr);", tableDataFullName(vTable.tableType));
                }
            }

            ps.println2("return mgr;");

            ps.println1("}");
            ps.println("}");
        }
    }


    private void genConfigSchema(VDb vdb) throws IOException {
        try (CachedIndentPrinter ps = createCode(new File(dstDir, "ConfigCodeSchema.java"), encoding)) {

            ps.println("package " + pkg + ";");
            ps.println();
            ps.println("import configgen.genjava.*;");
            ps.println();

            ps.println("public class ConfigCodeSchema {");
            ps.println();

            GenJavaCodeSchema.print(GenSchema.parse(vdb), ps);
            ps.println("}");

        }
    }

}
