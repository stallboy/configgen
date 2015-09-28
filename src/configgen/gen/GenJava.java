package configgen.gen;

import configgen.Utils;
import configgen.define.Field;
import configgen.type.*;
import configgen.value.CfgVs;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenJava extends Generator {
    private File dstDir;
    private String pkg;
    private String encoding;

    public GenJava(Path dir, CfgVs value, Context ctx) {
        super(dir, value, ctx);
        String _dir = ctx.get("dir", ".");
        pkg = ctx.get("pkg", "config");
        encoding = ctx.get("encoding", "GBK");
        ctx.end();
        dstDir = Paths.get(_dir).resolve(pkg.replace('.', '/')).toFile();
    }

    @Override
    public void gen() throws IOException {
        CachedFileOutputStream.removeOtherFiles(dstDir);
        mkdirs(dstDir);

        for (TBean b : value.type.tbeans.values()) {
            genBean(b, null);
        }
        for (Cfg c : value.type.cfgs.values()) {
            genBean(c.tbean, c);
        }
        genCSV();
        genCSVLoader();

        CachedFileOutputStream.doRemoveFiles();
    }

    private static void mkdirs(File path) {
        if (!path.exists()) {
            if (!path.mkdirs()) {
                System.out.println("mkdirs fail: " + path);
            }
        }
    }

    private class Name {
        String pkg;
        String className;
        String fullName;
        String path;

        Name(String name) {
            String[] seps = name.split("\\.");
            String[] pks = Arrays.copyOf(seps, seps.length - 1);
            if (pks.length == 0)
                pkg = GenJava.this.pkg;
            else
                pkg = GenJava.this.pkg + "." + String.join(".", pks);

            className = Utils.upper1(seps[seps.length - 1]);
            fullName = pkg + "." + className;
            if (pks.length == 0)
                path = className + ".java";
            else
                path = String.join("/", pks) + "/" + className + ".java";
        }
    }

    private void genBean(TBean tbean, Cfg cfg) throws IOException {
        Name name = new Name(tbean.define.name);
        File javaFile = dstDir.toPath().resolve(name.path).toFile();
        mkdirs(javaFile.getParentFile());

        try (PrintStream ps = Utils.cachedPrintStream(javaFile, encoding)) {
            genBean(tbean, cfg, name, new TabPrintStream(ps));
        }
    }

    private void genBean(TBean tbean, Cfg cfg, Name name, TabPrintStream ps) throws IOException {
        ps.println("package " + name.pkg + ";");
        ps.println();

        boolean isEnumFull = (cfg != null && cfg.value.isEnum && !cfg.value.isEnumPart);
        boolean isEnumPart = (cfg != null && cfg.value.isEnum && cfg.value.isEnumPart);

        ps.println((isEnumFull ? "public enum " : "public class ") + name.className + " {");

        //static enum
        if (isEnumFull) {
            String es = String.join("," + System.lineSeparator() + "    ", cfg.value.enumNames.stream()
                    .map(String::toUpperCase).collect(Collectors.toList()));
            ps.println1(es + ";");
            ps.println();
        } else if (isEnumPart) {
            cfg.value.enumNames.forEach(s ->
                    ps.println1("private static " + name.className + " " + s.toUpperCase() + "_;"));
            ps.println();

            cfg.value.enumNames.forEach(s ->
                    ps.println1("public static " + name.className + " " + s.toUpperCase() + "() { return " + s.toUpperCase() + "_; }"));
            ps.println();
        }

        //field
        tbean.fields.forEach((n, t) -> {
            ps.println1("private " + type(t) + " " + Utils.lower1(n) + initialValue(t) + ";");
            t.constraint.refs.forEach(r -> ps.println1("private " + refType(t, r) + " " + refName(r) + refInitialValue(t) + ";"));
        });

        tbean.mRefs.forEach(m -> ps.println1("private " + fullName(m.ref) + " " + refName(m) + ";"));
        tbean.listRefs.forEach(l -> ps.println1("private java.util.List<" + fullName(l.ref) + "> " + refName(l) + ";"));
        ps.println();

        //constructor
        if (cfg == null) {
            ps.println1("public " + name.className + "() {");
            ps.println1("}");
            ps.println();

            ps.println1("public " + name.className + "(" + formalParams(tbean.fields) + ") {");
            tbean.fields.forEach((n, t) -> ps.println2("this." + Utils.lower1(n) + " = " + Utils.lower1(n) + ";"));
            ps.println1("}");
            ps.println();
        }

        //getter
        tbean.fields.forEach((n, t) -> {
            Field f = tbean.define.fields.get(n);
            if (!f.desc.isEmpty()) {
                ps.println1("/**");
                ps.println1("* " + f.desc);
                ps.println1("*/");
            }


            ps.println1("public " + type(t) + " get" + Utils.upper1(n) + "() {");
            ps.println2("return " + Utils.lower1(n) + ";");
            ps.println1("}");
            ps.println();

            t.constraint.refs.forEach(r -> {
                ps.println1("public " + refType(t, r) + " " + Utils.lower1(refName(r)) + "() {");
                ps.println2("return " + refName(r) + ";");
                ps.println1("}");
                ps.println();
            });
        });

        tbean.mRefs.forEach(m -> {
            ps.println1("public " + fullName(m.ref) + " " + Utils.lower1(refName(m)) + "() {");
            ps.println2("return " + refName(m) + ";");
            ps.println1("}");
            ps.println();
        });

        tbean.listRefs.forEach(l -> {
            ps.println1("public java.util.List<" + fullName(l.ref) + "> " + Utils.lower1(refName(l)) + "() {");
            ps.println2("return " + refName(l) + ";");
            ps.println1("}");
            ps.println();
        });

        //hashCode, equals
        Map<String, Type> keys = cfg != null ? cfg.keys : tbean.fields;
        if (!isEnumFull) {
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
        ps.println2("return \"(\" + " + String.join(" + \",\" + ", tbean.fields.keySet().stream().map(Utils::lower1).collect(Collectors.toList())) + " + \")\";");
        ps.println1("}");
        ps.println();

        //_parse
        ps.println1((cfg == null ? "public " : "") + name.className + " _parse(java.util.List<String> data) {");
        if (tbean.define.compress) {
            ps.println2("data = " + pkg + ".CSV.parseList(data.get(0));");
        }

        boolean hasA = false;
        int begin = 0;
        for (Map.Entry<String, Type> f : tbean.fields.entrySet()) {
            String n = f.getKey();
            Type t = f.getValue();

            int end = begin + t.columnSpan();
            if (t instanceof TPrimitive) {
                ps.println2(Utils.lower1(n) + " = " + parsePrimitive(t, "data.get(" + begin + ")") + ";");

            } else if (t instanceof TBean) {
                ps.println2(Utils.lower1(n) + "._parse(data.subList(" + begin + ", " + end + "));");

            } else if (t instanceof TList) {
                TList type = (TList) t;
                if (type.count == 0) {
                    ps.println2("for (String e : " + pkg + ".CSV.parseList(data.get(" + begin + ")))");
                    ps.println3(Utils.lower1(n) + ".add(" + parsePrimitive(type.value, "e") + ");");
                } else {
                    int vs = type.value.columnSpan();
                    for (int i = 0; i < type.count; i++) {
                        int b = begin + i * vs;
                        String value = parseType(type.value, "a", b, vs);
                        String prefix = hasA ? "" : "String ";
                        hasA = true;
                        ps.println2(prefix + "a = data.get(" + b + ");");
                        ps.println2("if (!a.isEmpty())");
                        ps.println3(Utils.lower1(n) + ".add(" + value + ");");
                    }
                }

            } else if (t instanceof TMap) {
                TMap type = (TMap) t;
                int ks = type.key.columnSpan();
                int vs = type.value.columnSpan();
                for (int i = 0; i < type.count; i++) {
                    int b = begin + i * (ks + vs);
                    String key = parseType(type.key, "a", b, ks);
                    String value = parseType(type.value, "data.get(" + (b + ks) + ")", b + ks, vs);

                    String prefix = hasA ? "" : "String ";
                    hasA = true;
                    ps.println2(prefix + "a = data.get(" + b + ");");
                    ps.println2("if (!a.isEmpty())");
                    ps.println3(Utils.lower1(n) + ".put(" + key + ", " + value + ");");
                }
            }
            begin = end;
        }
        ps.println2("return this;");
        ps.println1("}");
        ps.println();
        //end _parse


        //_resolve
        if (tbean.hasRef()) {
            ps.println1((cfg == null ? "public " : "") + "void _resolve() {");

            for (Map.Entry<String, Type> f : tbean.fields.entrySet()) {
                String n = f.getKey();
                Type t = f.getValue();
                if (t.hasRef()) {
                    if (t instanceof TList) {
                        TList tt = (TList) t;
                        ps.println2(Utils.lower1(n) + ".forEach( e -> {");
                        if (tt.value instanceof TBean && tt.value.hasRef()) {
                            ps.println3("e._resolve();");
                        }

                        for (SRef sr : t.constraint.refs) {
                            ps.println3(fullName(sr.ref) + " r = " + fullName(sr.ref) + ".get(e);");
                            ps.println3("java.util.Objects.requireNonNull(r);");
                            ps.println3(refName(sr) + ".add(r);");
                        }
                        ps.println2("});");
                    } else if (t instanceof TMap) {
                        TMap tt = (TMap) t;
                        ps.println2(Utils.lower1(n) + ".forEach( (k, v) -> {");
                        if (tt.key instanceof TBean && tt.key.hasRef()) {
                            ps.println3("k._resolve();");
                        }
                        if (tt.value instanceof TBean && tt.value.hasRef()) {
                            ps.println3("v._resolve();");
                        }

                        for (SRef sr : t.constraint.refs) {
                            String k = "k";
                            if (sr.keyRef != null) {
                                ps.println3(fullName(sr.keyRef) + " rk = " + fullName(sr.keyRef) + ".get(k);");
                                ps.println3("java.util.Objects.requireNonNull(rk);");
                                k = "rk";
                            }
                            String v = "v";
                            if (sr.ref != null) {
                                ps.println3(fullName(sr.ref) + " rv = " + fullName(sr.ref) + ".get(v);");
                                ps.println3("java.util.Objects.requireNonNull(rv);");
                                v = "rv";
                            }
                            ps.println3(refName(sr) + ".put(" + k + ", " + v + ");");
                        }
                        ps.println2("});");
                    } else {
                        if (t instanceof TBean && t.hasRef()) {
                            ps.println2(Utils.lower1(n) + "._resolve();");
                        }

                        for (SRef sr : t.constraint.refs) {
                            ps.println2(refName(sr) + " = " + fullName(sr.ref) + ".get(" + Utils.lower1(n) + ");");
                            if (!sr.nullable)
                                ps.println2("java.util.Objects.requireNonNull(" + refName(sr) + ");");
                        }

                    }
                }
            } //end fields

            tbean.mRefs.forEach(m -> {
                ps.println2(refName(m) + " = " + fullName(m.ref) + ".get(" + actualParams(m.define.keys) + ");");
                if (!m.define.nullable)
                    ps.println2("java.util.Objects.requireNonNull(" + refName(m) + ");");
            });

            tbean.listRefs.forEach(l -> {
                ps.println2(fullName(l.ref) + ".all().forEach( v -> {");
                List<String> eqs = new ArrayList<>();
                for (int i = 0; i < l.keys.length; i++) {
                    String k = l.keys[i];
                    String rk = l.refKeys[i];
                    eqs.add(equal("v.get" + Utils.upper1(rk) + "()", Utils.lower1(k), tbean.fields.get(k)));
                }
                ps.println3("if (" + String.join(" && ", eqs) + ")");
                ps.println4(refName(l) + ".add(v);");
                ps.println2("});");
            });

            ps.println1("}");
            ps.println();
        } //end _resolve


        if (cfg != null) {
            if (keys.size() > 1) {
                //static Key class
                ps.println1("private static class Key {");
                keys.forEach((n, t) -> ps.println2("private " + type(t) + " " + Utils.lower1(n) + ";"));
                ps.println();

                ps.println2("Key(" + formalParams(keys) + ") {");
                keys.forEach((n, t) -> ps.println3("this." + Utils.lower1(n) + " = " + Utils.lower1(n) + ";"));
                ps.println2("}");
                ps.println();

                ps.println2("@Override");
                ps.println2("public int hashCode() {");
                ps.println3("return " + hashCodes(keys) + ";");
                ps.println2("}");
                ps.println();

                ps.println2("@Override");
                ps.println2("public boolean equals(Object other) {");
                ps.println3("if (null == other || !(other instanceof Key))");
                ps.println4("return false;");
                ps.println3("Key o = (Key) other;");
                ps.println3("return " + equals(keys) + ";");
                ps.println2("}");

                ps.println1("}");
                ps.println();
            }

            //static All
            ps.println1("private static final java.util.Map<" + (keys.size() > 1 ? "Key" : boxType(keys.values().iterator().next())) + ", " + name.className + "> All = new java.util.LinkedHashMap<>();");
            ps.println();

            //static get
            ps.println1("public static " + name.className + " get(" + formalParams(keys) + ") {");
            ps.println2("return All.get(" + actualParamsKey(keys, "") + ");");
            ps.println1("}");
            ps.println();

            //static all
            ps.println1("public static java.util.Collection<" + name.className + "> all() {");
            ps.println2("return All.values();");
            ps.println1("}");
            ps.println();

            //static initialize
            ps.println1("static void initialize(java.util.List<java.util.List<String>> dataList) {");
            ps.println2("java.util.List<Integer> indexes = java.util.Arrays.asList(" + String.join(", ", cfg.value.columnIndexes.stream().map(String::valueOf).collect(Collectors.toList())) + ");");
            ps.println2("for (java.util.List<String> row : dataList) {");
            ps.println3("java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());");
            if (isEnumFull) {
                ps.println3(name.className + " self = valueOf(row.get(" + cfg.value.enumColumnIndex + ").trim().toUpperCase())._parse(data);");
                ps.println3("All.put(" + actualParamsKey(keys, "self.") + ", self);");
            } else {
                ps.println3(name.className + " self = new " + name.className + "()._parse(data);");
                ps.println3("All.put(" + actualParamsKey(keys, "self.") + ", self);");
                if (isEnumPart) {
                    ps.println3("String name = row.get(" + cfg.value.enumColumnIndex + ").trim().toUpperCase();");
                    ps.println3("switch (name) {");
                    cfg.value.enumNames.forEach(s -> {
                        ps.println4("case \"" + s.toUpperCase() + "\":");
                        ps.println5(s.toUpperCase() + "_ = self;");
                        ps.println5("break;");
                    });
                    ps.println3("}");
                }
            }
            ps.println2("}");

            if (isEnumFull) {
                ps.println2("if (values().length != all().size()) ");
                ps.println3("throw new RuntimeException(\"Enum Uncompleted: " + name.className + "\");");
            } else if (isEnumPart) {
                cfg.value.enumNames.forEach(s -> ps.println2("ava.util.Objects.requireNonNull(" + s.toUpperCase() + "_);"));
            }
            ps.println1("}");
            ps.println();

            //static resolve
            if (tbean.hasRef()) {
                ps.println1("static void resolve() {");
                ps.println2("all().forEach(" + name.className + "::_resolve);");
                ps.println1("}");
                ps.println();
            }
        } //end cfg != null
        ps.println("}");
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
            public String visit(TText type) {
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
                return " = \"\"";
            }

            @Override
            public String visit(TText type) {
                return " = \"\"";
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
                return " = new " + fullName(type) + "()";
            }
        });
    }

    private String fullName(TBean tbean) {
        return new Name(tbean.define.name).fullName;
    }

    private String fullName(Cfg cfg) {
        return fullName(cfg.tbean);
    }

    private String refType(Type t, SRef ref) {
        if (t instanceof TList) {
            return "java.util.List<" + fullName(ref.ref) + ">";
        } else if (t instanceof TMap) {
            return "java.util.Map<"
                    + (ref.keyRef != null ? fullName(ref.keyRef) : boxType(((TMap) t).key)) + ", "
                    + (ref.ref != null ? fullName(ref.ref) : boxType(((TMap) t).value)) + ">";
        } else {
            return fullName(ref.ref);
        }
    }

    private String refName(SRef sr) {
        return (sr.nullable ? "NullableRef" : "Ref") + Utils.upper1(sr.name);
    }

    private String refName(MRef mr) {
        return (mr.define.nullable ? "NullableRef" : "Ref") + Utils.upper1(mr.define.name);
    }

    private String refName(ListRef lr) {
        return "ListRef" + Utils.upper1(lr.name);
    }

    private String refInitialValue(Type t) {
        if (t instanceof TList) {
            return " =  java.util.ArrayList<>()";
        } else if (t instanceof TMap) {
            return " = java.util.LinkedHashMap<>();";
        } else {
            return "";
        }
    }

    private String formalParams(Map<String, Type> fs) {
        return String.join(", ", fs.entrySet().stream().map(e -> type(e.getValue()) + " " + Utils.lower1(e.getKey())).collect(Collectors.toList()));
    }

    private static String actualParams(String[] keys) {
        return String.join(", ", Arrays.asList(keys).stream().map(Utils::lower1).collect(Collectors.toList()));
    }


    private static String actualParamsKey(Map<String, Type> keys, String pre) {
        String p = String.join(", ", keys.entrySet().stream().map(e -> pre + Utils.lower1(e.getKey())).collect(Collectors.toList()));
        return keys.size() > 1 ? "new Key(" + p + ")" : p;
    }

    private String hashCodes(Map<String, Type> fs) {
        return String.join(" + ", fs.entrySet().stream().map(e -> hashCode(e.getKey(), e.getValue())).collect(Collectors.toList()));
    }

    private static String hashCode(String name, Type t) {
        String n = Utils.lower1(name);
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
            public String visit(TText type) {
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
        return String.join(" && ", fs.entrySet().stream().map(e -> equal(Utils.lower1(e.getKey()), "o." + Utils.lower1(e.getKey()), e.getValue())).collect(Collectors.toList()));
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
            public Boolean visit(TText type) {
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

    private String parsePrimitive(Type type, String content) {
        if (type instanceof TInt) {
            return pkg + ".CSV.parseInt(" + content + ")";
        } else if (type instanceof TBool) {
            return pkg + ".CSV.parseBoolean(" + content + ")";
        } else if (type instanceof TFloat) {
            return pkg + ".CSV.parseFloat(" + content + ")";
        } else if (type instanceof TLong) {
            return pkg + ".CSV.parseLong(" + content + ")";
        } else {
            return content;
        }
    }

    private String parseType(Type type, String a, int s, int len) {
        if (type instanceof TPrimitive) {
            return parsePrimitive(type, a);
        } else if (type instanceof TBean) {
            return parseBean(type, "data.subList(" + s + ", " + (s + len) + ")");
        } else {
            throw new IllegalStateException();
        }
    }

    private String parseBean(Type type, String content) {
        return "new " + fullName((TBean) type) + "()._parse(" + content + ")";
    }


    private void genCSV() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/export/CSV.java");
             BufferedReader br = new BufferedReader(new InputStreamReader(is != null ? is : new FileInputStream(
                     "src/configgen/CSV.java"), "GBK"));
             PrintStream ps = Utils.cachedPrintStream(new File(dstDir, "CSV.java"), encoding)) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (line.equals("package configgen;"))
                    line = "package " + pkg + ";";
                ps.println(line);
            }
        }
    }

    private void genCSVLoader() throws IOException {
        try (PrintStream ps = Utils.cachedPrintStream(new File(dstDir, "CSVLoader.java"), encoding)) {

            ps.println("package " + pkg + ";");
            ps.println();

            ps.println("import java.nio.file.Path;");
            ps.println("import java.util.Set;");
            ps.println("import java.util.LinkedHashSet;");
            ps.println();

            ps.println("public class CSVLoader {");
            ps.println();

            ps.println("	public static Set<String> load(Path zipPath, String encoding) throws Exception {");
            ps.println("		Set<String> loaded = CSV.load(zipPath, encoding);");
            ps.println("		Set<String> configs = new LinkedHashSet<>();");
            value.cfgvs.forEach((k, v) -> ps.println("		configs.add(\"" + k + "\");"));
            ps.println("		configs.removeAll(loaded);");
            ps.println("		return configs;");
            ps.println("	}");
            ps.println();

            ps.println("	public static void main(String[] args) throws Exception {");
            ps.println("		System.out.println(\"missed: \" + load(java.nio.file.Paths.get(\"configdata.zip\"), \"GBK\"));");
            ps.println("	}");

            ps.println("}");
        }
    }
}
