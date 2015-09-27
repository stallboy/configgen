package configgen.gen;

import configgen.Main;
import configgen.Utils;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ToJava {

    private final Collection<Bean> beans;
    private final Collection<Config> configs;
    private final Path configDir;
    private Path codeDir;
    private String codePackage;

    public ToJava(ConfigCollection root, Path configDir) {
        this.beans = root.getBeans();
        this.configs = root.getConfigs();
        this.configDir = configDir;
    }

    public void generateCode(Path _codeDir, String _codePackage) throws IOException {
        codePackage = _codePackage;
        codeDir = _codeDir.resolve(_codePackage.replace('.', '/'));

        CachedFileOutputStream.removeOtherFiles(codeDir.toFile());
        if (!codeDir.toFile().exists()) {
            if (!codeDir.toFile().mkdirs()) {
                System.out.println("config: mkdirs fail: " + codeDir);
            }
        }

        Main.verbose("generate CSV");
        generateCSV();
        Main.verbose("generate CSVLoader");
        generateCSVLoader();
        for (Bean b : beans) {
            Main.verbose("generate bean " + b.getName());
            generateBean(b);
        }
        for (Config c : configs) {
            Main.verbose("generate config " + c.getName());
            generateBean(c.getBean());
        }

        CachedFileOutputStream.doRemoveFiles();
    }

    public void generateData(File dataFile) throws IOException {
        Main.verbose("generate " + dataFile);
        try (final ZipOutputStream zos = new ZipOutputStream(new CheckedOutputStream(new CachedFileOutputStream(dataFile), new CRC32()))) {

            Files.walkFileTree(configDir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fn = configDir.relativize(file).toString();
                    if (fn.endsWith(".csv")) {
                        zos.putNextEntry(new ZipEntry(fn));
                        Files.copy(file, zos);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private void generateCSV() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/configgen/CSV.java");
             BufferedReader br = new BufferedReader(new InputStreamReader(is != null ? is : new FileInputStream(
                     "src/configgen/CSV.java"), "GBK"));
             PrintStream ps = Main.outputPs(codeDir.resolve("CSV.java"))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (line.equals("package configgen;"))
                    line = "package " + codePackage + ";";
                ps.println(line);
            }
        }
    }

    private void generateCSVLoader() throws IOException {
        try (PrintStream ps = Main.outputPs(codeDir.resolve("CSVLoader.java"))) {

            ps.println("package " + codePackage + ";");
            ps.println();

            ps.println("import java.nio.file.Path;");
            ps.println("import java.util.Set;");
            ps.println("import java.util.LinkedHashSet;");
            ps.println();

            ps.println("public class CSVLoader {");
            ps.println();

            ps.println("	/*");
            ps.println("	 * return missed csv");
            ps.println("	 */");
            ps.println("	public static Set<String> load(Path zipPath, String charsetName) throws Exception {");
            ps.println("		Set<String> loaded = CSV.load(zipPath, charsetName);");
            ps.println("		Set<String> configs = new LinkedHashSet<>();");
            configs.forEach(c -> ps.println("		configs.add(\"" + c.getName() + "\");"));
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

    private void generateBean(Bean bean) throws IOException {
        String className = bean.N();

        Path javaPath = codeDir.resolve(bean.PathN() + ".java");
        File parentDir = javaPath.toFile().getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.out.println("config: mkdirs fail: " + parentDir);
            }
        }

        try (PrintStream ps = Main.outputPs(javaPath)) {
            ps.println("package " + bean.NS() + ";");
            ps.println();

            Config config = bean.getConfig();
            boolean isEnumFull = (config != null && config.isEnumFull());
            boolean isEnumPart = (config != null && config.isEnumPart());

            ps.println((isEnumFull ? "public enum " : "public class ") + className + " {");

            //static enum
            if (isEnumFull) {
                String es = String.join("," + System.lineSeparator() + "	", config.getEnumNames().stream()
                        .map(String::toUpperCase).collect(Collectors.toList()));
                ps.println("	" + es + ";");
                ps.println();
            } else if (isEnumPart) {
                config.getEnumNames().forEach(s ->
                        ps.println("	private static " + className + " " + s.toUpperCase() + "_;"));
                ps.println();

                config.getEnumNames().forEach(s ->
                        ps.println("	public static " + className + " " + s.toUpperCase() + "()    { return " + s.toUpperCase() + "_; }"));
                ps.println();
            }

            //field
            for (Field f : bean.getFields()) {
                ps.println("	private " + type(f) + " " + f.n() + initial(f, false) + ";");
                if (f._hasRef())
                    ps.println("	private " + refType(f) + " " + f.RefN() + initial(f, true) + ";");
            }
            for (Ref c : bean.getRefs()) {
                ps.println("	private " + c.getRef().FullN() + " " + c.RefN() + ";");
            }
            ps.println();

            List<Field> keys = new ArrayList<>(config != null ? config.getKeyFields() : bean.getFields());

            //constructor
            if (config == null) {
                ps.println("	public " + className + "() {");
                ps.println("	}");
                ps.println();

                ps.println("	public " + className + "(" + formalParams(keys) + ") {");
                keys.forEach(f -> ps.println("		this." + f.n() + " = " + f.n() + ";"));
                ps.println("	}");
                ps.println();
            }

            //getter
            for (Field f : bean.getFields()) {
                if (!f.getDesc().isEmpty()) {
                    ps.println("	/**");
                    ps.println("	 * " + f.getDesc());
                    ps.println("	 */");
                }
                ps.println("	public " + type(f) + " get" + f.N() + "() {");
                ps.println("		return " + f.n() + ";");
                ps.println("	}");
                ps.println();

                if (f._hasRef()) {
                    ps.println("	public " + refType(f) + " " + f.refN() + "() {");
                    ps.println("		return " + f.RefN() + ";");
                    ps.println("	}");
                    ps.println();
                }
            }
            for (Ref c : bean.getRefs()) {
                ps.println("	public " + c.getRef().FullN() + " " + c.refN() + "() {");
                ps.println("		return " + c.RefN() + ";");
                ps.println("	}");
                ps.println();
            }

            //hashCode, equals
            if (!isEnumFull) {
                ps.println("	@Override");
                ps.println("	public int hashCode() {");
                ps.println("		return " + hashCodes(keys) + ";");
                ps.println("	}");
                ps.println();

                ps.println("	@Override");
                ps.println("	public boolean equals(Object other) {");
                ps.println("		if (!(other instanceof " + className + "))");
                ps.println("			return false;");
                ps.println("		" + className + " o = (" + className + ") other;");
                ps.println("		return " + equals(keys, "", "o.") + ";");
                ps.println("	}");
                ps.println();
            }

            //toString
            ps.println("    @Override");
            ps.println("    public String toString() {");
            ps.println("        return \"(\" + " + String.join(" + \",\" + ", bean.getFields().stream().map(Field::n).collect(Collectors.toList())) + " + \")\";");
            ps.println("    }");
            ps.println();

            //_parse
            String publicStr = config == null ? "public " : "";
            ps.println("    " + publicStr + className + " _parse(java.util.List<String> data) {");
            if (bean.isCompress()) {
                ps.println("        data = " + codePackage + ".CSV.parseList(data.get(0));");
            }

            boolean hasA = false;
            int begin = 0;
            for (Field f : bean.getFields()) {
                Type t = f.getType();

                int end = begin + t.columnSpan();
                if (t instanceof TBool) {
                    ps.println("        " + f.n() + " = " + parsePrimitive((TBool) t, "data.get(" + begin + ")") + ";");

                } else if (t instanceof Bean) {
                    ps.println("		" + f.n() + "._parse(data.subList(" + begin + ", " + end + "));");

                } else if (t instanceof TList) {
                    TList type = (TList) t;
                    if (type.isCompress()) {
                        ps.println("        for (String e : " + codePackage + ".CSV.parseList(data.get(" + begin + ")))");
                        ps.println("            " + f.n() + ".add(" + parsePrimitive((TBool) type.value, "e") + ");");
                    } else {
                        int vs = type.value.columnSpan();
                        for (int i = 0; i < type.count; i++) {
                            int b = begin + i * vs;
                            String value;
                            if (type.value instanceof TBool) {
                                value = parsePrimitive((TBool) type.value, "a");
                            } else if (type.value instanceof Bean) {
                                value = parseBean((Bean) type.value, "data.subList(" + b + ", " + (b + vs) + ")");
                            } else {
                                throw new IllegalStateException();
                            }

                            String prefix = hasA ? "" : "String ";
                            hasA = true;

                            ps.println("        " + prefix + "a = data.get(" + b + ");");
                            ps.println("        if (!a.isEmpty())");
                            ps.println("            " + f.n() + ".add(" + value + ");");

                        }
                    }

                } else if (t instanceof TMap) {
                    TMap type = (TMap) t;
                    int ks = type.key.columnSpan();
                    int vs = type.value.columnSpan();
                    for (int i = 0; i < type.count; i++) {
                        int b = begin + i * (ks + vs);

                        String key;
                        if (type.key instanceof TBool) {
                            key = parsePrimitive((TBool) type.key, "a");
                        } else if (type.key instanceof Bean) {
                            key = parseBean((Bean) type.key, "data.subList(" + b + ", " + (b + ks) + ")");
                        } else {
                            throw new IllegalStateException();
                        }

                        String value;
                        if (type.value instanceof TBool) {
                            value = parsePrimitive((TBool) type.value, "data.get(" + (b + ks) + ")");
                        } else if (type.value instanceof Bean) {
                            value = parseBean((Bean) type.value, "data.subList(" + (b + ks) + ", " + (b + ks + vs) + ")");
                        } else {
                            throw new IllegalStateException();
                        }

                        String prefix = hasA ? "" : "String ";
                        hasA = true;

                        ps.println("        " + prefix + "a = data.get(" + b + ");");
                        ps.println("        if (!a.isEmpty())");
                        ps.println("            " + f.n() + ".put(" + key + ", " + value + ");");
                    }
                }
                begin = end;
            }

            ps.println("        return this;");
            ps.println("    }");
            ps.println();
            //end _parse


            //_resolve
            if (bean.hasRef()) {
                ps.println("    " + publicStr + "void _resolve() {");
                bean.getFields().stream().filter(Field::hasRef).forEach(f -> {
                    Type t = f.getType();

                    if (t instanceof TList) {
                        TList tt = (TList) t;
                        ps.println("        " + f.n() + ".forEach( e -> {");

                        if (tt.value.hasRef()) {
                            ps.println("            e._resolve();");
                        }
                        if (f._hasRef()) {
                            String refclazz = f.getRef().FullN();
                            ps.println("            " + refclazz + " r = " + refclazz + ".get(e);");
                            ps.println("            java.util.Objects.requireNonNull(r);");
                            ps.println("            " + f.RefN() + ".add(r);");
                        }
                        ps.println("        });");

                    } else if (t instanceof TMap) {
                        TMap tt = (TMap) t;
                        ps.println("        " + f.n() + ".forEach( (k, v) -> {");
                        if (tt.key.hasRef()) {
                            ps.println("            k._resolve();");
                        }
                        if (tt.value.hasRef()) {
                            ps.println("            v._resolve();");
                        }

                        if (f._hasRef()) {
                            String k = "k";
                            if (f.getKeyRef() != null) {
                                String refclazz = f.getKeyRef().FullN();
                                ps.println("            " + refclazz + " rk = " + refclazz + ".get(k);");
                                ps.println("            java.util.Objects.requireNonNull(rk);");
                                k = "rk";
                            }

                            String v = "v";
                            if (f.getRef() != null) {
                                String refclazz = f.getRef().FullN();
                                ps.println("            " + refclazz + " rv = " + refclazz + ".get(v);");
                                ps.println("            java.util.Objects.requireNonNull(rv);");
                                v = "rv";
                            }

                            ps.println("            " + f.RefN() + ".put(" + k + ", " + v + ");");
                        }
                        ps.println("        });");

                    } else {
                        if (t.hasRef()) {
                            ps.println("        " + f.n() + "._resolve();");
                        }

                        if (f.getListRef() != null) {
                            ps.println("        " + f.getListRef().getBean().FullN() + ".all().forEach( v -> {");
                            String eq = equal("v.get" + f.getListRef().N() + "()", f.n(), f);
                            ps.println("            if (" + eq + ")");
                            ps.println("                " + f.RefN() + ".add(v);");
                            ps.println("        });");
                        } else if (f._hasRef()) {
                            ps.println("        " + f.RefN() + " = " + f.getRef().FullN() + ".get(" + f.n() + ");");
                            if (!f.isRefNullable())
                                ps.println("        java.util.Objects.requireNonNull(" + f.RefN() + ");");
                        }
                    }
                });

                bean.getRefs().forEach(c -> {
                    ps.println("        " + c.RefN() + " = " + c.getRef().FullN() + ".get(" + actualParamsRaw(c.getFields(), "") + ");");
                    if (!c.isRefNullable())
                        ps.println("        java.util.Objects.requireNonNull(" + c.RefN() + ");");
                });
                ps.println("    }");
                ps.println();
            }
            //end _resolve

            //static Key class
            if (config != null) {

                if (keys.size() > 1) {
                    ps.println("	private static class Key {");
                    for (Field f : keys) {
                        String n = Utils.lower1(f.getName());
                        ps.println("		private " + type(f) + " " + n + ";");
                    }
                    ps.println();

                    ps.println("		Key(" + formalParams(keys) + ") {");
                    for (Field f : keys) {
                        String n = Utils.lower1(f.getName());
                        ps.println("			this." + n + " = " + n + ";");
                    }
                    ps.println("		}");
                    ps.println();

                    ps.println("		@Override");
                    ps.println("		public int hashCode() {");
                    ps.println("			return " + hashCodes(keys) + ";");
                    ps.println("		}");
                    ps.println();

                    ps.println("		@Override");
                    ps.println("		public boolean equals(Object other) {");
                    ps.println("			if (!(other instanceof Key))");
                    ps.println("				return false;");
                    ps.println("			Key o = (Key) other;");
                    ps.println("			return " + equals(keys, "", "o.") + ";");
                    ps.println("		}");
                    ps.println("	}");
                    ps.println();
                }

                //static All
                ps.println("	private static final java.util.Map<" + keysBoxType(keys) + ", " + className + "> All = new java.util.LinkedHashMap<>();");
                ps.println();

                //static get
                ps.println("	public static " + className + " get(" + formalParams(keys) + ") {");
                ps.println("		return All.get(" + actualParams(keys, "") + ");");
                ps.println("	}");
                ps.println();

                //static all
                ps.println("	public static java.util.Collection<" + className + "> all() {");
                ps.println("		return All.values();");
                ps.println("	}");
                ps.println();

                //static initialize
                ps.println("    static void initialize(java.util.List<java.util.List<String>> dataList) " + numberThrowStr + "{");
                ps.println("        java.util.List<Integer> columnList = java.util.Arrays.asList(" +
                        String.join(", ", config.getColumnList().stream().map(String::valueOf).collect(Collectors.toList())) + ");");
                ps.println("        for (java.util.List<String> row : dataList) {");
                ps.println("            java.util.List<String> data = columnList.stream().map(row::get).collect(java.util.stream.Collectors.toList());");
                if (isEnumFull) {
                    ps.println("            " + className + " self = valueOf(row.get(" + config.getEnumDataHead().getColumn() + ").trim().toUpperCase())._parse(data);");
                    ps.println("            All.put(" + actualParams(keys, "self.") + ", self);");
                } else {
                    ps.println("            " + className + " self = new " + className + "()._parse(data);");
                    ps.println("            All.put(" + actualParams(keys, "self.") + ", self);");
                    if (isEnumPart) {
                        ps.println("            String ename = row.get(" + config.getEnumDataHead().getColumn() + ").trim().toUpperCase();");
                        ps.println("            switch (ename) {");
                        config.getEnumNames().forEach(s -> {
                            ps.println("                case \"" + s.toUpperCase() + "\":");
                            ps.println("                    " + s.toUpperCase() + "_ = self;");
                            ps.println("                    break;");
                        });
                        ps.println("            }");
                    }
                }
                ps.println("        }");

                if (isEnumFull) {
                    ps.println("		if (values().length != all().size()) ");
                    ps.println("			throw new RuntimeException(\"Enum Uncomplete: " + className + "\");");
                } else if (isEnumPart) {
                    config.getEnumNames().forEach(s -> ps.println("        java.util.Objects.requireNonNull(" + s.toUpperCase() + "_);"));
                }

                ps.println("    }");
                ps.println();


                //static resolve
                if (bean.hasRef()) {
                    ps.println("    static void resolve() {");
                    ps.println("        all().forEach(" + className + "::_resolve);");
                    ps.println("    }");
                    ps.println();
                }
            } //end config != null

            ps.println("}");
        }

    }

    private static String actualParams(List<Field> fs, String pre) {
        String p = actualParamsRaw(fs, pre);
        return fs.size() > 1 ? "new Key(" + p + ")" : p;
    }

    private static String actualParamsRaw(List<Field> fs, String pre) {
        return String.join(", ", fs.stream().map(f -> pre + f.n()).collect(Collectors.toList()));
    }

    private static String formalParams(List<Field> fs) {
        return String.join(", ", fs.stream().map(ToJava::formalParam).collect(Collectors.toList()));
    }

    private static String formalParam(Field f) {
        return type(f) + " " + actualParam(f);
    }

    private static String actualParam(Field f) {
        return f.n();
    }

    private static String hashCodes(List<Field> fs) {
        return String.join(" + ", fs.stream().map(ToJava::hashCode).collect(Collectors.toList()));
    }

    private static String hashCode(Field f) {
        String n = f.n();
        Type t = f.getType();
        if (t instanceof TBool) {
            switch ((TBool) t) {
                case BOOL:
                    return "Boolean.hashCode(" + n + ")";
                case INT:
                    return n;
                case LONG:
                    return "Long.hashCode(" + n + ")";
                case FLOAT:
                    return "Float.hashCode(" + n + ")";
            }
        }
        return n + ".hashCode()";
    }

    private static String equals(List<Field> fs, String pre1, String pre2) {
        String[] eqs = new String[fs.size()];
        int i = 0;
        for (Field k : fs) {
            String n = k.n();
            eqs[i++] = equal(pre1 + n, pre2 + n, k);
        }
        return String.join(" && ", eqs);
    }

    private static String equal(String a, String b, Field f) {
        return useEqual(f.getType()) ? a + ".equals(" + b + ")" : a + " == " + b;
    }

    private static boolean useEqual(Type t) {
        if (t instanceof TBool) {
            switch ((TBool) t) {
                case BOOL:
                case FLOAT:
                case INT:
                case LONG:
                    return false;
            }
        }
        return true;
    }

    private static String initial(Field f, boolean ref) {
        return f.getType().accept(new TVisitor<String>() {
            @Override
            public String visit(TBool type) {
                if (ref) {
                    if (f.getListRef() != null)
                        return " = new java.util.ArrayList<>()";
                } else {
                    if (type == TBool.STRING || type == TBool.TEXT)
                        return " = \"\"";
                }
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
            public String visit(Bean type) {
                if (ref) {
                    if (f.getListRef() != null)
                        return " = new java.util.ArrayList<>()";
                } else {
                    return " = new " + type.FullN() + "()";
                }
                return "";
            }
        });
    }

    private String parsePrimitive(TBool type, String content) {
        switch (type) {
            case BOOL:
                return codePackage + ".CSV.parseBoolean(" + content + ")";
            case FLOAT:
                return codePackage + ".CSV.parseFloat(" + content + ")";
            case INT:
                return codePackage + ".CSV.parseInt(" + content + ")";
            case LONG:
                return codePackage + ".CSV.parseLong(" + content + ")";
            default:
                return content;
        }
    }

    private String parseBean(Bean type, String content) {
        return "new " + type.FullN() + "()._parse(" + content + ")";
    }


    private static String refType(Field f) {
        return f.getType().accept(new TVisitor<String>() {
            @Override
            public String visit(TBool type) {
                return f.getListRef() != null ? "java.util.List<" + f.getListRef().getBean().FullN() + ">" : f.getRef().FullN();
            }

            @Override
            public String visit(TList type) {
                return "java.util.List<" + f.getRef().FullN() + ">";
            }

            @Override
            public String visit(TMap type) {
                return "java.util.Map<"
                        + (f.getKeyRef() != null ? f.getKeyRef().FullN() : boxType(type.key)) + ", "
                        + (f.getRef() != null ? f.getRef().FullN() : boxType(type.value)) + ">";
            }

            @Override
            public String visit(Bean type) {
                return f.getListRef() != null ? "java.util.List<" + f.getListRef().getBean().FullN() + ">" : f.getRef().FullN();
            }
        });
    }

    private static String type(Field f) {
        return type(f.getType());
    }

    private static String type(Type t) {
        return t.accept(new TVisitor<String>() {
            @Override
            public String visit(TBool type) {
                switch (type) {
                    case BOOL:
                        return "boolean";
                    case FLOAT:
                        return "float";
                    case INT:
                        return "int";
                    case LONG:
                        return "long";
                    default:
                        return "String";
                }
            }

            @Override
            public String visit(TList type) {
                return "java.util.List<" + boxType(type.value) + ">";
            }

            @Override
            public String visit(TMap type) {
                return "java.util.Map<" + boxType(type.key) + ", " + boxType(type.value) + ">";
            }

            @Override
            public String visit(Bean type) {
                return type.FullN();
            }
        });
    }

    private static String keysBoxType(List<Field> keys) {
        return keys.size() > 1 ? "Key" : boxType(keys.get(0).getType());
    }

    private static String boxType(Type t) {
        return t.accept(new TVisitor<String>() {
            @Override
            public String visit(TBool type) {
                switch (type) {
                    case BOOL:
                        return "Boolean";
                    case FLOAT:
                        return "Float";
                    case INT:
                        return "Integer";
                    case LONG:
                        return "Long";
                    default:
                        return "String";
                }
            }

            @Override
            public String visit(TList type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(TMap type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String visit(Bean type) {
                return type.FullN();
            }
        });
    }


}
