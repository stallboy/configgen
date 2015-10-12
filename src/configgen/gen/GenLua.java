package configgen.gen;

import configgen.define.Field;
import configgen.type.*;
import configgen.value.CfgVs;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GenLua extends Generator {
    private CfgVs value;
    private File dstDir;
    private String pkg;
    private String encoding;

    public GenLua() {
        providers.put("lua", this);
        Context.providers.put("lua", "cs,dir:.,pkg:cfg,encoding:UTF-8    add ,own:x if need, cooperate with -gen bin, -gen pack");
    }

    @Override
    public void generate(Path configDir, CfgVs _value, Context ctx) throws IOException {
        String _dir = ctx.get("dir", ".");
        pkg = ctx.get("pkg", "cfg");
        encoding = ctx.get("encoding", "UTF-8");
        String own = ctx.get("own", null);
        ctx.end();
        dstDir = Paths.get(_dir).resolve(pkg.replace('.', '/')).toFile();
        value = own != null ? extract(_value, own) : _value;

        CachedFileOutputStream.removeOtherFiles(dstDir);
        mkdirs(dstDir);

        copyFile("CSV.cs");
        copyFile("CSVLoader.cs");
        copyFile("LoadErrors.cs");
        copyFile("KeyedList.cs");
        genCSVLoaderDoLoad();

        try (PrintStream ps = cachedPrintStream(new File(dstDir, "_beans.lua"), encoding)) {
            TabPrintStream tps = new TabPrintStream(ps);
            for (TBean b : value.type.tbeans.values()) {
                genBean(b, null, b.define.name.toLowerCase(), tps);
            }

            tps.println("return {");
            for (TBean b : value.type.tbeans.values()) {
                tps.println1(b.define.name.toLowerCase() + " = " + b.define.name.toLowerCase() + ",");
            }
            tps.println("}");
        }

        for (Cfg c : value.type.cfgs.values()) {
            Name name = new Name(pkg, c.tbean.define.name);
            File csFile = dstDir.toPath().resolve(name.path).toFile();
            mkdirs(csFile.getParentFile());
            try (PrintStream ps = cachedPrintStream(csFile, encoding)) {
                genBean(c.tbean, c, name.className, new TabPrintStream(ps));
            }
        }

        try (PrintStream ps = cachedPrintStream(new File(dstDir, "_cfgs.lua"), encoding)) {
            genCfgs(new TabPrintStream(ps));
        }

        CachedFileOutputStream.doRemoveFiles();
    }

    private static class Name {
        final String pkg;
        final String className;
        final String fullName;
        final String path;

        Name(String topPkg, String configName) {
            String[] seps = configName.split("\\.");
            String[] pks = new String[seps.length - 1];
            for (int i = 0; i < pks.length; i++)
                pks[i] = seps[i].toLowerCase();
            className = seps[seps.length - 1].toLowerCase();

            if (pks.length == 0)
                pkg = topPkg;
            else
                pkg = topPkg + "." + String.join(".", pks);

            if (pkg.isEmpty())
                fullName = className;
            else
                fullName = pkg + "." + className;

            if (pks.length == 0)
                path = className + ".lua";
            else
                path = String.join("/", pks) + "/" + className + ".lua";
        }
    }

    private void genCfgs(TabPrintStream ps) {
        ps.println("local " + pkg + " = {}");
        Set<String> created = new HashSet<>();
        created.add(pkg);

        for (Cfg c : value.type.cfgs.values()) {
            Name name = new Name(pkg, c.tbean.define.name);
            if (created.add(name.pkg)) {
                ps.println(name.pkg + " = {}");
            }
            ps.println(name.fullName + " = require(\"" + name.fullName + "\")");
        }
        ps.println();



        ps.println("return " + pkg);
    }

    private void genBean(TBean tbean, Cfg cfg, String className, TabPrintStream ps) {
        if (cfg != null && tbean.hasSubBean()) {
            ps.println("local Beans = require(\"" + pkg + "._beans\")");
            ps.println();
        }

        ps.println("local " + className + " = {}");

        //static enum
        if (cfg != null) {
            ps.println(className + ".all = {}");
            cfg.value.enumNames.forEach(e -> ps.println(className + "." + e + " = nil"));
            ps.println();
        }

        // static _create
        ps.println("function " + className + "._create(os)");
        ps.println1("local o = {}");
        tbean.fields.forEach((n, t) -> {
            Field f = tbean.define.fields.get(n);
            String c = f.desc.isEmpty() ? "" : " -- " + f.desc;

            if (t instanceof TList) {
                ps.println1("o." + lower1(n) + " = {}" + c);
                ps.println1("for _ = 1, os.readUInt16() do");
                ps.println2("table.insert(o." + lower1(n) + ", " + _create(((TList) t).value) + ")");
                ps.println1("end");
            } else if (t instanceof TMap) {
                ps.println1("o." + lower1(n) + " = {}" + c);
                ps.println1("for _ = 1, os.readUInt16() do");
                ps.println2("o." + lower1(n) + "[" + _create(((TMap) t).key) + "] = " + _create(((TMap) t).value));
                ps.println1("end");
            } else {
                ps.println1("o." + lower1(n) + " = " + _create(t) + c);
            }

            t.constraint.refs.forEach(r -> ps.println1("o." + refName(r) + " = nil"));
        });
        tbean.mRefs.forEach(m -> ps.println1("o." + refName(m) + " = nil"));
        tbean.listRefs.forEach(l -> ps.println1("o." + refName(l) + " = {}"));
        ps.println1("return o");
        ps.println("end");
        ps.println();

        Map<String, Type> keys = cfg != null ? cfg.keys : tbean.fields;
        String csv = "\"" + tbean.define.name + "\"";
        if (cfg != null) {
            //static get
            ps.println("function " + className + "._get(" + formalParams(keys) + ")");
            ps.println1("return " + className + ".all[" + actualParams(keys, "") + "]");
            ps.println("end");
            ps.println();

            //static _initialize
            ps.println("function " + className + "._initialize(os, errors)");
            ps.println1("for _ = 1, os.readUInt16() do");
            ps.println2("local v = _create(os)");
            if (cfg.value.isEnum) {
                ps.println2("if #(v." + cfg.define.enumStr + ") > 0 then");
                ps.println3(className + "[v." + lower1(cfg.define.enumStr) + "] = v");
                ps.println2("end");
            }
            ps.println2(className + ".all[" + actualParams(keys, "v") + "] = v");
            ps.println1("end");
            if (cfg.value.isEnum) {
                cfg.value.enumNames.forEach(e -> {
                    ps.println1("if " + className + "." + e + " == nil then");
                    ps.println2("errors.enumNil(" + csv + ", \"" + e + "\");");
                    ps.println1("end");
                });
            }
            ps.println("end");
            ps.println();

            ps.println("return " + className);
        }
    }

    private String formalParams(Map<String, Type> fs) {
        return String.join(", ", fs.keySet().stream().map(Generator::lower1).collect(Collectors.toList()));
    }

    private String actualParams(Map<String, Type> keys, String prefix) {
        return String.join(" ..", keys.keySet().stream().map(n -> prefix + lower1(n)).collect(Collectors.toList()));
    }

    private String refName(SRef sr) {
        return (sr.nullable ? "NullableRef" : "Ref") + upper1(sr.name);
    }

    private String refName(MRef mr) {
        return (mr.define.nullable ? "NullableRef" : "Ref") + upper1(mr.define.name);
    }

    private String refName(ListRef lr) {
        return "ListRef" + upper1(lr.name);
    }

    private String fullName(TBean tbean) {
        return tbean.isBean ? "Beans." + tbean.define.name.toLowerCase() : new Name(pkg, tbean.define.name).fullName;
    }

    private String fullName(Cfg cfg) {
        return fullName(cfg.tbean);
    }

    private String _create(Type t) {
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return "os.readBool()";
            }

            @Override
            public String visit(TInt type) {
                return "os.readInt32()";
            }

            @Override
            public String visit(TLong type) {
                return "os.readInt64()";
            }

            @Override
            public String visit(TFloat type) {
                return "os.readSingle()";
            }

            @Override
            public String visit(TString type) {
                return type.subtype == TString.Subtype.STRING ? "os.readString()" : "os.readText()";
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
        });
    }

    private void copyFile(String file) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/support/" + file);
             BufferedReader br = new BufferedReader(new InputStreamReader(is != null ? is : new FileInputStream("src/support/" + file), "GBK"));
             PrintStream ps = cachedPrintStream(new File(dstDir, file), encoding)) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                ps.println(line);
            }
        }
    }

    private void genCSVLoaderDoLoad() throws IOException {
        try (PrintStream stream = cachedPrintStream(new File(dstDir, "CSVLoaderDoLoad.cs"), encoding)) {
            TabPrintStream ps = new TabPrintStream(stream);
            ps.println("using System.Collections.Generic;");
            ps.println("using System.IO;");
            ps.println();

            ps.println("namespace Config");
            ps.println("{");

            ps.println1("public static partial class CSVLoader {");
            ps.println();
            ps.println2("public static LoadErrors DoLoad(List<BinaryReader> byterList, Dictionary<string, Dictionary<ushort, string>> allTextMap)");
            ps.println2("{");
            ps.println3("var errors = new LoadErrors();");
            ps.println3("var configNulls = new List<string>");
            ps.println3("{");
            for (String name : value.type.cfgs.keySet()) {
                ps.println4("\"" + name + "\",");
            }
            ps.println3("};");

            ps.println3("foreach (var byter in byterList)");
            ps.println3("{");
            ps.println3("for(;;)");
            ps.println3("{");
            ps.println4("try");
            ps.println4("{");
            ps.println5("var csv = CSV.ReadString(byter);");
            ps.println5("var count = byter.ReadUInt16();");
            ps.println5("Dictionary<ushort, string> textMap;");
            ps.println5("allTextMap.TryGetValue(csv, out textMap);");

            ps.println5("switch(csv)");
            ps.println5("{");

            value.type.cfgs.forEach((name, cfg) -> {
                ps.println6("case \"" + name + "\":");
                ps.println7("configNulls.Remove(csv);");
                ps.println7(fullName(cfg.tbean) + ".Initialize(count, byter, textMap, errors);");
                ps.println7("break;");
            });

            ps.println6("default:");
            ps.println7("errors.ConfigDataAdd(csv);");
            ps.println7("break;");
            ps.println5("}");

            ps.println4("}");
            ps.println4("catch (EndOfStreamException)");
            ps.println4("{");
            ps.println5("break;");
            ps.println4("}");

            ps.println3("}");
            ps.println3("}");

            ps.println3("foreach (var csv in configNulls)");
            ps.println4("errors.ConfigNull(csv);");

            value.type.cfgs.forEach((n, c) -> {
                if (c.tbean.hasRef()) {
                    ps.println3(fullName(c) + ".Resolve(errors);");
                }
            });

            ps.println3("return errors;");
            ps.println2("}");
            ps.println();
            ps.println1("}");
            ps.println("}");
            ps.println();
        }
    }

}
