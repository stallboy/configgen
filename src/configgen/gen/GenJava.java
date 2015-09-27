package configgen.gen;

import configgen.Utils;
import configgen.type.*;
import configgen.value.CfgVs;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
            genBean(tbean, cfg, name, ps);
        }
    }

    private void genBean(TBean tbean, Cfg cfg, Name name, PrintStream ps) throws IOException {
        ps.println("package " + name.pkg + ";");
        ps.println();

        boolean isEnumFull = (cfg != null && cfg.value.isEnum && !cfg.value.isEnumPart);
        boolean isEnumPart = (cfg != null && cfg.value.isEnum && cfg.value.isEnumPart);

        ps.println((isEnumFull ? "public enum " : "public class ") + name.className + " {");

        //static enum
        if (isEnumFull) {
            String es = String.join("," + System.lineSeparator() + "	", cfg.value.enumNames.stream()
                    .map(String::toUpperCase).collect(Collectors.toList()));
            ps.println("	" + es + ";");
            ps.println();
        } else if (isEnumPart) {
            cfg.value.enumNames.forEach(s ->
                    ps.println("	private static " + name.className + " " + s.toUpperCase() + "_;"));
            ps.println();

            cfg.value.enumNames.forEach(s ->
                    ps.println("	public static " + name.className + " " + s.toUpperCase() + "()    { return " + s.toUpperCase() + "_; }"));
            ps.println();
        }

        //field
        tbean.fields.forEach((n, t) -> {
            ps.println("	private " + type(t) + " " + Utils.lower1(n) + initialValue(t) + ";");
            t.constraint.refs.forEach(r -> ps.println("	private " + refType(t, r) + " " + refName(r) + refInitialValue(t) + ";"));
        });

        tbean.mRefs.forEach(m -> ps.println("	private " + fullName(m.ref) + " " + refName(m) + ";"));
        ps.println();
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
                return new Name(type.define.name).fullName;
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
                return " = new " + new Name(type.define.name).fullName + "()";
            }
        });
    }

    private String fullName(Cfg cfg) {
        return new Name(cfg.define.bean.name).fullName;
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

    private String refName(SRef ref) {
        return (ref.nullable ? "nullableRef" : "ref") + Utils.upper1(ref.name);
    }

    private String refName(MRef mr) {
        return (mr.define.nullable ? "nullableRef" : "ref") + Utils.upper1(mr.define.name);
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
