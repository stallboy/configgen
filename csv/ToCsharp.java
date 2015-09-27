package configgen.gen;

import configgen.Main;
import configgen.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ToCsharp {
    private final Collection<Bean> beans;
    private final Collection<Config> configs;
    private final Bean mainToolBean;
    private final Set<Bean> toolBeans;

    public ToCsharp(ConfigCollection root) {
        if (Main.tool == null) {
            beans = root.OWNBeans();
            configs = root.OWNConfigs();
            mainToolBean = null;
            toolBeans = new HashSet<>();
        } else {
            mainToolBean = root.TOOLMainBean();
            toolBeans = root.TOOLBeans();
            Set<Bean> depBeans = root.TOOLDependBeans();
            beans = depBeans.stream().filter(b -> b.getConfig() == null).collect(Collectors.toList());
            configs = depBeans.stream().filter(b -> b.getConfig() != null).map(Bean::getConfig).collect(Collectors.toList());
        }
    }

    public void generateCode(Path codeDir) throws IOException {
        CachedFileOutputStream.removeOtherFiles(codeDir.toFile());
        if (!codeDir.toFile().exists()) {
            if (!codeDir.toFile().mkdirs()) {
                System.out.println("config: mkdirs fail: " + codeDir);
            }
        }

        copyFile(codeDir, "CSV.cs");
        copyFile(codeDir, "CSVLoader.cs");
        copyFile(codeDir, "LoadErrors.cs");
        copyFile(codeDir, "KeyedList.cs");

        generateCSVLoaderDoLoad(codeDir);

        for (Bean b : beans) {
            Main.verbose("generate bean " + b);
            generateBean(codeDir, b, false);
        }

        for (Config c : configs) {
            Main.verbose("generate config " + c);
            generateBean(codeDir, c.getBean(), false);
        }

        for (Bean b : toolBeans) {
            Main.verbose("generate tool bean " + b);
            generateBean(codeDir, b, true);
        }

        CachedFileOutputStream.doRemoveFiles();
    }

    public void generateData(File dataFile) throws IOException {
        File byteFile = new File(dataFile.getParent(), "csv.byte");
        File textFile = new File(dataFile.getParent(), "text.csv");
        try (DataOutputStream byter = new DataOutputStream(new CachedFileOutputStream(byteFile));
             OutputStreamWriter texter = new OutputStreamWriter(new CachedFileOutputStream(textFile), "UTF-8")) {
            CSVWriter writer = new CSVWriter(byter, texter);
            for (Config c : configs) {
                writer.addConfig(c);
            }
        }

        try (ZipOutputStream zos = new ZipOutputStream(new CheckedOutputStream(new CachedFileOutputStream(dataFile), new CRC32()))) {
            zos.putNextEntry(new ZipEntry("csv.byte"));
            Files.copy(byteFile.toPath(), zos);
            zos.putNextEntry(new ZipEntry("text.csv"));
            Files.copy(textFile.toPath(), zos);
        }

        byteFile.delete();
        textFile.delete();
    }

    private void copyFile(Path codeDir, String file) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/configgen/" + file);
             BufferedReader br = new BufferedReader(new InputStreamReader(is != null ? is : new FileInputStream(file), "GBK"));
             PrintStream ps = Main.outputPs(codeDir.resolve(file))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                ps.println(line);
            }
        }
    }

    private void generateCSVLoaderDoLoad(Path codeDir) throws IOException {
        try (PrintStream ps = Main.outputPs(codeDir.resolve("CSVLoaderDoLoad.cs"))) {
            ps.println("using System.Collections.Generic;");
            ps.println("using System.IO;");
            ps.println();

            ps.println("namespace Config");
            ps.println("{");

            ps.println("    public static partial class CSVLoader {");
            ps.println();
            ps.println("        public static LoadErrors DoLoad(BinaryReader byter, Dictionary<string, Dictionary<ushort, string>> allTextMap)");
            ps.println("        {");
            ps.println("            var errors = new LoadErrors();");
            ps.println("            var configNulls = new List<string>");
            ps.println("            {");
            for (Config cfg : configs) {
                ps.println("                \"" + cfg.getName() + "\",");
            }
            ps.println("            };");

            ps.println("            for(;;)");
            ps.println("            {");
            ps.println("                try");
            ps.println("                {");
            ps.println("                    var csv = CSV.ReadString(byter);");
            ps.println("                    var count = byter.ReadUInt16();");
            ps.println("                    Dictionary<ushort, string> textMap;");
            ps.println("                    allTextMap.TryGetValue(csv, out textMap);");

            ps.println("                    switch(csv)");
            ps.println("                    {");

            for (Config cfg : configs) {
                ps.println("                        case \"" + cfg.getName() + "\":");
                ps.println("                            configNulls.Remove(csv);");
                ps.println("                            " + cfg.getBean().FullN() + ".Initialize(count, byter, textMap, errors);");
                ps.println("                            break;");
            }

            ps.println("                        default:");
            ps.println("                            errors.ConfigDataAdd(csv);");
            ps.println("                            break;");
            ps.println("                    }");

            ps.println("                }");
            ps.println("                catch (EndOfStreamException)");
            ps.println("                {");
            ps.println("                    break;");
            ps.println("                }");

            ps.println("            }");

            ps.println("            foreach (var csv in configNulls)");
            ps.println("                errors.ConfigNull(csv);");

            configs.stream().filter(cfg -> cfg.getBean().HASREF()).forEach(cfg -> ps.println("            " + cfg.getBean().FullN() + ".Resolve(errors);"));

            ps.println("            return errors;");
            ps.println("        }");
            ps.println();
            ps.println("    }");
            ps.println("}");
            ps.println();
        }
    }

    private void generateBean(Path codeDir, Bean bean, boolean forTool) throws IOException {
        Path csPath = codeDir.resolve(bean.PathN() + ".cs");
        File parentDir = csPath.toFile().getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.out.println("config: mkdirs fail: " + parentDir);
            }
        }

        try (PrintStream ps = Main.outputPs(csPath)) {
            if (forTool)
                generateBeanCodeForTool(ps, bean);
            else
                generateBeanCode(ps, bean);
        }
    }

    private void generateBeanCodeForTool(PrintStream ps, Bean bean) {
        String className = bean.N();
        Config config = bean.getConfig();

        ps.println("using System;");
        ps.println("using System.Collections.Generic;");
        ps.println("using System.IO;");
        ps.println("using System.Xml.Serialization;");
        ps.println();

        ps.println("namespace " + bean.NS());
        ps.println("{");

        ps.println("    public partial class " + className);
        ps.println("    {");
        // property
        if (bean == mainToolBean) {
            ps.println("        [XmlIgnore]");
            ps.println("        public string filePath;");
            ps.println();
        }

        for (Field f : bean.getFields()) {
            String c = f.getDesc().isEmpty() ? "" : " // " + f.getDesc();

            boolean isRefSubToolBean = (f.getRef() != null && toolBeans.contains(f.getRef()) && f.getRef() != mainToolBean);

            if (isRefSubToolBean)
                ps.println("        [XmlIgnore]");

            ps.println("        public " + type(f) + " " + f.N() + "; " + c);

            if (f._hasRef()) {
                if (!isRefSubToolBean)
                    ps.println("        [XmlIgnore]");
                ps.println("        public " + refType(f) + " " + f.RefN() + ";");
            }
        }
        ps.println();

        for (Ref c : bean.getRefs()) {
            ps.println("        [XmlIgnore]");
            ps.println("        public " + c.getRef().FullN() + " " + c.RefN() + ";");
        }
        ps.println();

        boolean isMainToolBean = (bean == mainToolBean);

        if (config != null) {
            List<Field> keys = new ArrayList<>();
            keys.add(bean.getFields().iterator().next());
            String keytype = type(keys.iterator().next().getType());
            ps.println("        public static Dictionary<" + keytype + ", " + className + "> all = new Dictionary<" + keytype + ", " + className + ">();");
            ps.println();

            // static get
            ps.println("        public static " + className + " Get(" + formalParams(keys) + ")");
            ps.println("        {");
            ps.println("            " + className + " v;");
            ps.println("            return all.TryGetValue(" + actualParams(keys) + ", out v) ? v : null;");
            ps.println("        }");
            ps.println();

            String add = isMainToolBean ? "public static void Add(" : "internal static void _add(";
            ps.println("        " + add + className + " self)");
            ps.println("        {");
            generate_addSelfForTool(ps, bean);
            ps.println("            all.Add(" + selfActualParams(keys) + ", self);");
            ps.println("        }");
            ps.println();

            if (isMainToolBean) {
                ps.println("        public static LoadErrors Initialize(string xmlDir)");
                ps.println("        {");
                ps.println("            var errors = new LoadErrors();");
                ps.println("            foreach (var f in Directory.GetFiles(xmlDir, \"*.xml\"))");
                ps.println("            {");
                ps.println("                var self = _loadFromXml(f);");
                ps.println("                Add(self);");
                ps.println("            }");
                ps.println("            foreach (var kv in all)");
                ps.println("            {");
                ps.println("                kv.Value._resolve(errors);");
                ps.println("            }");
                ps.println("            return errors;");
                ps.println("        }");
                ps.println();

                ps.println("        public static void SaveToCsv(string csvDir)");
                ps.println("        {");
                for (Bean b : toolBeans)
                    ps.println("            " + b.FullN() + "._saveToCsv(csvDir);");
                ps.println("        }");
                ps.println();

                ps.println("        public void SaveToXml() {");
                ps.println("            XmlSerializer xs = new XmlSerializer(typeof (" + className + "));");
                ps.println("            using (TextWriter w = new StreamWriter(new FileStream(filePath, FileMode.Create)))");
                ps.println("            {");
                ps.println("                xs.Serialize(w, this);");
                ps.println("            }");
                ps.println("        }");
                ps.println();

                ps.println("        internal static " + className + " _loadFromXml(string filePath) {");
                ps.println("            XmlSerializer xs = new XmlSerializer(typeof (" + className + "));");
                ps.println("            using (TextReader r = new StreamReader(new FileStream(filePath, FileMode.Open)))");
                ps.println("            {");
                ps.println("                var v =  (" + className + ") xs.Deserialize(r);");
                ps.println("                v.filePath = filePath;");
                ps.println("                return v;");
                ps.println("            }");
                ps.println("        }");
                ps.println();
            }
        }

        generate_resolveForBean(ps, bean, true);
        generate_toCsv(ps, bean);

        ps.println("        internal static void _saveToCsv(string csvDir)");
        ps.println("        {");
        String[] seps = bean.getName().split("\\.");
        ps.println("            var filePath = string.Join(\"/\", new[]{csvDir, " +
                String.join(", ", Arrays.stream(seps).map(s -> "\"" + s + "\"").collect(Collectors.toList())) + "}) + \".csv\";");
        ps.println("            var data = new List<List<string>>();");

        ps.println("            foreach (var kv in all)");
        ps.println("            {");
        ps.println("                var line = new List<string>();");
        ps.println("                kv.Value._toCsv(line);");
        ps.println("                data.Add(line);");
        ps.println("            }");
        ps.println("            Config.CSVSaver.KeepHeaderAndSave(data, filePath);");
        ps.println("        }");
        ps.println();


        ps.println("    }");
        ps.println("}");

    }

    private void generate_toCsv(PrintStream ps, Bean bean) {
        ps.println("        internal void _toCsv(List<string> line)");
        ps.println("        {");

        String _fline= "line";
        if (bean.isCompress()) {
            ps.println("            var subline = new List<string>();");
            _fline = "subline";
        }

        String fline = _fline;
        for (Field f : bean.getFields()) {
            Type t = f.getType();
            t.accept(new Visitor() {
                @Override
                public void visit(TBool type) {
                    ps.println("            " + fline + ".Add(" + getTypePrimitiveString(type, f.N()) + ");");
                }

                @Override
                public void visit(TList type) {

                    ps.println("            if (" + f.N() + " != null)");
                    ps.println("            {");
                    //not null
                    String _listline = "line";
                    if (type.isCompress()) {
                        _listline = "str" + f.N();
                        ps.println("                var " + _listline + " = new List<string>();");
                    }
                    ps.println("                foreach (var e in " + f.N() + ")");
                    String listline = _listline;
                    type.value.accept(new Visitor() {
                        @Override
                        public void visit(TBool type) {
                            ps.println("                    " + listline + ".Add(" + getTypePrimitiveString(type, "e") + ");");
                        }

                        @Override
                        public void visit(TList type) {
                            throw new RuntimeException("unsupported list in list");
                        }

                        @Override
                        public void visit(TMap type) {
                            throw new RuntimeException("unsupported map in list");
                        }

                        @Override
                        public void visit(Bean type) {
                            ps.println("                    e._toCsv(" + listline + ");");
                        }
                    });

                    if (type.isCompress()) {
                        ps.println("                " + fline + ".Add(Config.CSVSaver.Format(" + listline + ", \";\"));");
                    } else {
                        ps.println("                for (var i = 0; i < (" + type.columnSpan()  + "-" + type.value.columnSpan() + "*"+ f.N() + ".Count); i++)");
                        ps.println("                {");
                        ps.println("                    " + fline + ".Add(\"\");");
                        ps.println("                }");
                    }
                    ps.println("            }");

                    ps.println("            else");
                    //null
                    ps.println("            {");
                    ps.println("                for (var i = 0; i < " + type.columnSpan() + "; i++)");
                    ps.println("                {");
                    ps.println("                    " + fline + ".Add(\"\");");
                    ps.println("                }");
                    ps.println("            }");
                }

                @Override
                public void visit(TMap type) {
                    throw new RuntimeException("unsupported map");
                }

                @Override
                public void visit(Bean type) {
                    ps.println("            if (" + f.N() + " != null)");
                    ps.println("            {");
                    ps.println("                " + f.N() + "._toCsv(" + fline + ");");
                    ps.println("            }");
                    ps.println("            else");
                    ps.println("            {");
                    ps.println("                for (var i = 0; i < " + bean.columnSpan() + "; i++");
                    ps.println("                {");
                    ps.println("                    " + fline + ".Add(\"\");");
                    ps.println("                }");
                    ps.println("            }");
                }
            });
        }

        if (bean.isCompress()) {
            ps.println("            line.Add(Config.CSVSaver.Format(subline, \";\"));");
        }
        ps.println("        }");
        ps.println();
    }


    private String getTypePrimitiveString(TBool type, String e) {
        switch (type) {
            case BOOL:
                return e + " ? \"1\" : \"\"";

            case INT:
            case LONG:
            case FLOAT:
                return e + " != 0 ? " + e + ".ToString() : \"\"";

            default:
                return e + " ?? \"\"";
        }
    }

    private void generate_addSelfForTool(PrintStream ps, Bean bean) {
        for (Field f : bean.getFields()) {
            boolean isRefSubToolBean = (f.getRef() != null && toolBeans.contains(f.getRef()) && f.getRef() != mainToolBean);

            if (isRefSubToolBean) {
                Type t = f.getType();
                String REFID = f.getRef().getFields().iterator().next().N();
                if (t instanceof TList) {
                    ps.println("            if (null != self." + f.RefN() + ")");
                    ps.println("            {");
                    ps.println("                self." + f.N() + " = new " + type(f) + "();");
                    ps.println("                foreach (var e in self." + f.RefN() + ")");
                    ps.println("                {");
                    ps.println("                    " + f.getRef().FullN() + "._add(e);");
                    ps.println("                    self." + f.N() + ".Add(e." + REFID + ");");
                    ps.println("                }");
                    ps.println("            }");

                } else if (t instanceof TMap) {
                    throw new RuntimeException("unsupported map");
                } else {
                    ps.println("            if (null != self." + f.RefN() + ") ");
                    ps.println("            {");
                    ps.println("                " + f.getRef().FullN() + "._add(self." + f.RefN() + ");");
                    ps.println("                self." + f.N() + " = self." + f.RefN() + "." + REFID + ";");
                    ps.println("            }");
                }
            }
        }
    }

    private void generateBeanCode(PrintStream ps, Bean bean) {
        String className = bean.N();
        Config config = bean.getConfig();

        ps.println("using System;");
        ps.println("using System.Collections.Generic;");
        ps.println("using System.IO;");
        ps.println();

        ps.println("namespace " + bean.NS());
        ps.println("{");

        boolean dependedBeanForToolCode = ( config == null && Main.tool != null);
        ps.println("    public partial class " + className);
        ps.println("    {");

        //static enum
        if (config != null && config.isEnum()) {
            config.getEnumNames().forEach(e -> ps.println("        public static " + className + " " + Utils.upper1(e) + " { get; private set; }"));
            ps.println();
        }


        // csv property
        for (Field f : bean.OWN()) {
            String c = f.getDesc().isEmpty() ? "" : " // " + f.getDesc();
            ps.println("        public " + type(f) + " " + f.N() + (dependedBeanForToolCode ? ";" : " { get; private set; }") + c);
            if (f._hasRef())
                ps.println("        public " + refType(f) + " " + f.RefN() + " { get; private set; }");
            ps.println();
        }
        for (Ref c : bean.getRefs()) {
            ps.println("        public " + c.getRef().FullN() + " " + c.RefN() + " { get; private set; }");
            ps.println();
        }
        ps.println();


        List<Field> keys = new ArrayList<>(config != null ? config.getKeyFields() : bean.getFields());

        //constructor
        if (config == null) {
            ps.println("        public " + className + "() {");
            ps.println("        }");
            ps.println();

            ps.println("        public " + className + "(" + formalParams(keys) + ") {");
            keys.forEach(f -> ps.println("            this." + f.N() + " = " + f.n() + ";"));
            ps.println("        }");
            ps.println();
        }

        //hash
        ps.println("        public override int GetHashCode()");
        ps.println("        {");
        ps.println("            return " + hashCodes(keys) + ";");
        ps.println("        }");
        ps.println();

        //equal
        ps.println("        public override bool Equals(object obj)");
        ps.println("        {");
        ps.println("            if (obj == null) return false;");
        ps.println("            if (obj == this) return true;");
        ps.println("            var o = obj as " + className + ";");
        ps.println("            return o != null && " + equals(keys) + ";");
        ps.println("        }");
        ps.println();

        //tostring
        ps.println("        public override string ToString()");
        ps.println("        {");
        ps.println("            return \"(\" + " + String.join(" + \",\" + ", bean.OWN().stream().map(f -> f.getType().accept(new TVisitor<String>() {
            @Override
            public String visit(TBool type) {
                return f.N();
            }

            @Override
            public String visit(TList type) {
                return "CSV.ToString(" + f.N() + ")";
            }

            @Override
            public String visit(TMap type) {
                return "CSV.ToString(" + f.N() + ")";
            }

            @Override
            public String visit(Bean type) {
                return f.N();
            }
        })).collect(Collectors.toList())) + " + \")\";");
        ps.println("        }");
        ps.println();

        if (dependedBeanForToolCode)
            generate_toCsv(ps, bean);

        if (config != null) {
            //static class Key
            if (keys.size() > 1) {
                ps.println("        class Key");
                ps.println("        {");
                for (Field f : keys)
                    ps.println("            readonly " + type(f) + " " + f.N() + ";");
                ps.println();

                ps.println("            public Key(" + formalParams(keys) + ")");
                ps.println("            {");
                for (Field f : keys)
                    ps.println("                this." + f.N() + " = " + f.n() + ";");
                ps.println("            }");
                ps.println();

                ps.println("            public override int GetHashCode()");
                ps.println("            {");
                ps.println("                return " + hashCodes(keys) + ";");
                ps.println("            }");

                //equal
                ps.println("            public override bool Equals(object obj)");
                ps.println("            {");
                ps.println("                if (obj == null) return false;");
                ps.println("                if (obj == this) return true;");
                ps.println("                var o = obj as Key;");
                ps.println("                return o != null && " + equals(keys) + ";");
                ps.println("            }");
                ps.println("        }");
                ps.println();
            }


            //static all
            String keytype = keys.size() > 1 ? "Key" : type(keys.iterator().next().getType());
            String alltype = "Config.KeyedList<" + keytype + ", " + className + ">";
            ps.println("	    static " + alltype + " all = null;");
            ps.println();

            // static get
            ps.println("        public static " + className + " Get(" + formalParams(keys) + ")");
            ps.println("        {");
            ps.println("            " + className + " v;");
            ps.println("            return all.TryGetValue(" + actualParams(keys) + ", out v) ? v : null;");
            ps.println("        }");
            ps.println();

            // static all
            ps.println("        public static List<" + className + "> All()");
            ps.println("        {");
            ps.println("            return all.OrderedValues;");
            ps.println("        }");
            ps.println();

            // static all
            ps.println("        public static List<" + className + "> Filter(Predicate<" + className + "> predicate)");
            ps.println("        {");
            ps.println("            var r = new List<" + className + ">();");
            ps.println("            foreach (var e in all.OrderedValues)");
            ps.println("            {");
            ps.println("                if (predicate(e))");
            ps.println("                    r.Add(e);");
            ps.println("            }");
            ps.println("            return r;");
            ps.println("        }");
            ps.println();


            //static initialize
            ps.println("        internal static void Initialize(int count, BinaryReader br, Dictionary<ushort, string> map, Config.LoadErrors errors)");
            ps.println("        {");
            ps.println("            all = new " + alltype + "();");
            ps.println("            for (var i = 0; i < count; i++) {");
            ps.println("                var self = _create(br, map);");
            ps.println("                all.Add(" + selfActualParams(keys) + ", self);");
            String csv = "\"" + bean.getName() + "\"";

            if (config.isEnum()) {
                String ef = config.getEnumField().N();
                ps.println("                if (self." + ef + ".Trim().Length == 0)");
                ps.println("                    continue;");
                ps.println("                switch(self." + ef + ".Trim())");
                ps.println("                {");
                config.getEnumNames().forEach(e -> {
                    ps.println("                    case \"" + e + "\":");
                    ps.println("                        if (" + Utils.upper1(e) + " != null)");
                    ps.println("                            errors.EnumDup(" + csv + ", self.ToString());");
                    ps.println("                        " + Utils.upper1(e) + " = self;");
                    ps.println("                        break;");
                });
                ps.println("                    default:");
                ps.println("                        errors.EnumDataAdd(" + csv + ", self.ToString());");
                ps.println("                        break;");
                ps.println("                }");
            }
            ps.println("            }");
            if (config.isEnum()) {
                config.getEnumNames().forEach(e -> {
                    ps.println("            if (" + Utils.upper1(e) + " == null)");
                    ps.println("                errors.EnumNull(" + csv + ", \"" + e + "\");");
                });
            }
            ps.println("        }");
            ps.println();

            //static resolve
            if (bean.HASREF()) {
                ps.println("        internal static void Resolve(Config.LoadErrors errors) {");
                ps.println("            foreach (var v in All())");
                ps.println("                v._resolve(errors);");
                ps.println("        }");
                ps.println();
            }
        } // end config != null

        //static create
        ps.println("        internal static " + className + " _create(BinaryReader br, Dictionary<ushort, string> map)");
        ps.println("        {");
        ps.println("            var self = new " + className + "();");
        for (Field f : bean.OWN()) {
            Type t = f.getType();
            if (t instanceof TList) {
                ps.println("            self." + f.N() + " = new " + type(f) + "();");
                ps.println("            for (var c = (int)br.ReadUInt16(); c > 0; c--)");
                ps.println("                self." + f.N() + ".Add(" + createPrimitiveOrBean(((TList) t).value) + ");");
            } else if (t instanceof TMap) {
                TMap type = (TMap) t;
                ps.println("            self." + f.N() + " = new " + type(f) + "();");
                ps.println("            for (var c = (int)br.ReadUInt16(); c > 0; c--)");
                ps.println("                self." + f.N() + ".Add(" + createPrimitiveOrBean(type.key) + ", " + createPrimitiveOrBean(type.value) + ");");
            } else {
                ps.println("            self." + f.N() + " = " + createPrimitiveOrBean(t) + ";");
            }
        }
        ps.println("            return self;");
        ps.println("        }");
        ps.println();
        //end static create

        //resolve
        if (bean.HASREF()) {
            generate_resolveForBean(ps, bean, false);
        }

        ps.println("    }");
        ps.println("}");
    }

    private void generate_resolveForBean(PrintStream ps, Bean bean, boolean forTool) {
        ps.println("        internal void _resolve(Config.LoadErrors errors)");
        ps.println("        {");

        String csv = "\"" + bean.getName() + "\"";
        bean.OWN().stream().filter(Field::hasRef).forEach(f -> generate_resolveForField(ps, f, forTool));

        bean.getRefs().stream().filter(Ref::ISOWN).forEach(c -> {
            ps.println("            " + c.RefN() + " = " + c.getRef().FullN() + ".Get(" + actualParamsRaw(c.getFields(), "") + ");");
            if (!c.isRefNullable())
                ps.println("            if (" + c.RefN() + " == null) errors.RefNull(" + csv + ", ToString(), \"" + c.toString() + "\", 0);");
        });

        ps.println("	    }");
        ps.println();
    }

    private void generate_resolveForField(PrintStream ps, Field f, boolean forTool) {
        String csv = "\"" + f.getBean().getName() + "\"";
        String field = "\"" + f.getName() + "\"";
        Type t = f.getType();
        boolean _hasCSVRef = f._hasRef();


        if (forTool) {
            boolean isRefSubToolBean = (f.getRef() != null && toolBeans.contains(f.getRef())) && f.getRef() != mainToolBean;
            _hasCSVRef = _hasCSVRef && !isRefSubToolBean;
        }

        if (t instanceof TList) {
            TList tt = (TList) t;
            if (_hasCSVRef || tt.value.hasRef()) {
                if (_hasCSVRef) {
                    ps.println("            " + f.RefN() + " = new " + refType(f) + "();");
                }

                if (forTool) {
                    ps.println("            if (null != " + f.N() + ") ");
                    ps.println("            {");
                }

                ps.println("            foreach (var e in " + f.N() + ")");
                ps.println("            {");
                if (tt.value.hasRef()) {
                    ps.println("                e._resolve(errors);");
                }
                if (_hasCSVRef) {
                    ps.println("                var r = " + f.getRef().FullN() + ".Get(e);");
                    ps.println("                if (r == null) errors.RefNull(" + csv + ", ToString() , " + field + ", e);");
                    ps.println("                " + f.RefN() + ".Add(r);");
                }
                ps.println("            }");

                if (forTool) {
                    ps.println("            }");
                }
            }

        } else if (t instanceof TMap) {
            TMap tt = (TMap) t;

            if (_hasCSVRef || tt.key.hasRef() || tt.value.hasRef()) {
                if (_hasCSVRef) {
                    ps.println("            " + f.RefN() + " = new " + refType(f) + "();");
                }


                if (forTool) {
                    ps.println("            if (null != " + f.N() + ") ");
                    ps.println("            {");
                }

                ps.println("            foreach (var kv in " + f.N() + ".Map)");
                ps.println("            {");
                if (tt.key.hasRef()) {
                    ps.println("                kv.Key._resolve(errors);");
                }
                if (tt.value.hasRef()) {
                    ps.println("                kv.Value._resolve(errors);");
                }

                if (_hasCSVRef) {
                    if (f.getKeyRef() != null) {
                        ps.println("                var k = " + f.getKeyRef().FullN() + ".Get(kv.Key);");
                        ps.println("                if (k == null) errors.RefKeyNull(" + csv + ", ToString(), " + field + ", kv.Key);");
                    } else {
                        ps.println("                var k = kv.Key;");
                    }

                    if (f.getRef() != null) {
                        ps.println("                var v = " + f.getRef().FullN() + ".Get(kv.Value);");
                        ps.println("                if (v == null) errors.RefNull(" + csv + ", ToString(), " + field + ", kv.Value);");
                    } else {
                        ps.println("                var v = kv.Value;");
                    }
                    ps.println("                " + f.RefN() + ".Add(k, v);");
                }
                ps.println("            }");

                if (forTool) {
                    ps.println("            }");
                }

            }


        } else {
            if (t.hasRef()) {
                ps.println("            " + f.N() + "._resolve(errors);");
            }
            if (f.getListRef() != null) {
                ps.println("            " + f.RefN() + " = new " + refType(f) + "();");
                ps.println("            foreach (var v in " + f.getListRef().getBean().FullN() + ".All())");
                ps.println("            {");
                ps.println("                if (v." + f.getListRef().N() + ".Equals(" + f.N() + "))");
                ps.println("                    " + f.RefN() + ".Add(v);");
                ps.println("            }");
            } else if (_hasCSVRef) {
                ps.println("            " + f.RefN() + " = " + f.getRef().FullN() + ".Get(" + f.N() + ");");
                if (!f.isRefNullable())
                    ps.println("            if (" + f.RefN() + " == null) errors.RefNull(" + csv + ", ToString(), " + field + ", " + f.N() + ");");
            }
        }

    }

    private static String createPrimitiveOrBean(Type t) {
        if (t instanceof TBool) {
            switch ((TBool) t) {
                case BOOL:
                    return "br.ReadBoolean()";
                case FLOAT:
                    return "br.ReadSingle()";
                case INT:
                    return "br.ReadInt32()";
                case LONG:
                    return "br.ReadInt64()";
                case STRING:
                    return "Config.CSV.ReadString(br)";
                case TEXT:
                    return "Config.CSV.ReadText(br, map)";
            }
        } else if (t instanceof Bean) {
            Bean bean = (Bean) t;
            return bean.FullN() + "._create(br, map)";
        }
        throw new IllegalStateException();
    }


    private static String actualParams(Collection<Field> fs) {
        String p = String.join(", ", fs.stream().map(Field::n).collect(Collectors.toList()));
        return fs.size() > 1 ? "new Key(" + p + ")" : p;
    }

    private static String selfActualParams(Collection<Field> fs) {
        String p = actualParamsRaw(fs, "self.");
        return fs.size() > 1 ? "new Key(" + p + ")" : p;
    }

    private static String actualParamsRaw(Collection<Field> fs, String pre) {
        return String.join(", ", fs.stream().map(f -> pre + f.N()).collect(Collectors.toList()));
    }

    private static String formalParams(Collection<Field> fs) {
        return String.join(", ", fs.stream().map(f -> type(f) + " " + f.n()).collect(Collectors.toList()));
    }

    private static String equals(Collection<Field> fs) {
        return String.join(" && ", fs.stream().map(f -> f.N() + ".Equals(o." + f.N() + ")").collect(Collectors.toList()));
    }

    private static String hashCodes(Collection<Field> fs) {
        return String.join(" + ", fs.stream().map(f -> f.N() + ".GetHashCode()").collect(Collectors.toList()));
    }

    private String refType(Field f) {
        Type t = f.getType();
        if (t instanceof TList) {
            return "List<" + f.getRef().FullN() + ">";
        } else if (t instanceof TMap) {
            return "KeyedList<"
                    + (f.getKeyRef() != null ? f.getKeyRef().FullN() : type(((TMap) t).key)) + ", "
                    + (f.getRef() != null ? f.getRef().FullN() : type(((TMap) t).value)) + ">";
        } else {
            if (f.getListRef() != null)
                return "List<" + f.getListRef().getBean().FullN() + ">";
            else
                return f.getRef().FullN();
        }
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
                        return "bool";
                    case FLOAT:
                        return "float";
                    case INT:
                        return "int";
                    case LONG:
                        return "long";
                    default:
                        return "string";
                }
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
            public String visit(Bean type) {
                return type.FullN();
            }
        });
    }

}
