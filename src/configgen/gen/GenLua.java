package configgen.gen;

import configgen.define.Field;
import configgen.type.*;
import configgen.value.CfgV;
import configgen.value.CfgVs;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class GenLua extends Generator {

    static void register() {
        providers.put("lua", new Provider() {
            @Override
            public Generator create(Parameter parameter) {
                return new GenLua(parameter);
            }

            @Override
            public String usage() {
                return "dir:.,pkg:cfg,encoding:UTF-8    add ,own:x if need, cooperate with -gen bin, -gen pack";
            }
        });
    }

    private String dir;
    private String pkg;
    private String encoding;
    private String own;
    private CfgVs value;

    public GenLua(Parameter parameter) {
        super(parameter);
        dir = parameter.get("dir", ".");
        pkg = parameter.getNotEmpty("pkg", "cfg");
        encoding = parameter.get("encoding", "UTF-8");
        own = parameter.get("own", null);
        parameter.end();
    }

    @Override
    public void generate(CfgVs _value) throws IOException {
        File dstDir = Paths.get(dir).resolve(pkg.replace('.', '/')).toFile();
        value = own != null ? extract(_value, own) : _value;
        try (TabPrintStream ps = createSource(new File(dstDir, "_beans.lua"), encoding)) {
            ps.println("local Beans = {}");
            for (TBean b : value.type.tbeans.values()) {
                ps.println("Beans." + className(b) + " = {}");
                genCreate(b, ps, "Beans.");
            }
            ps.println("return Beans");
        }
        for (CfgV c : value.cfgvs.values()) {
            Name name = new Name(pkg, c.name);
            File csFile = dstDir.toPath().resolve(name.path).toFile();
            try (TabPrintStream ps = createSource(dstDir.toPath().resolve(name.path).toFile(), encoding)) {
                genCfg(c.type, c, ps);
            }
        }
        try (TabPrintStream ps = createSource(new File(dstDir, "_cfgs.lua"), encoding)) {
            genCfgs(ps);
        }
        CachedFileOutputStream.deleteOtherFiles(dstDir);
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

    private void genCfg(Cfg cfg, CfgV cfgv, TabPrintStream ps) {
        String className = className(cfg.tbean);
        if (cfg.tbean.hasSubBean()) {
            ps.println("local Beans = require(\"" + pkg + "._beans\")");
            ps.println();
        }

        ps.println("local " + className + " = {}");
        ps.println(className + ".all = {}");
        cfgv.enumNames.forEach(e -> ps.println(className + "." + e + " = nil"));
        ps.println();
        genCreate(cfg.tbean, ps, "");

        //static get
        ps.println("function " + className + ".get(" + formalParams(cfg.keys) + ")");
        ps.println1("return " + className + ".all[" + actualParams(cfg.keys, "") + "]");
        ps.println("end");
        ps.println();

        //static _initialize
        ps.println("function " + className + "._initialize(os, errors)");
        ps.println1("for _ = 1, os:ReadSize() do");
        ps.println2("local v = " + className + "._create(os)");
        if (cfgv.isEnum) {
            ps.println2("if #(v." + lower1(cfg.define.enumStr) + ") > 0 then");
            ps.println3(className + "[v." + lower1(cfg.define.enumStr) + "] = v");
            ps.println2("end");
        }
        ps.println2(className + ".all[" + actualParams(cfg.keys, "v.") + "] = v");
        ps.println1("end");
        if (cfgv.isEnum) {
            cfgv.enumNames.forEach(e -> {
                ps.println1("if " + className + "." + e + " == nil then");
                ps.println2("errors.enumNil(\"" + cfg.tbean.define.name + "\", \"" + e + "\");");
                ps.println1("end");
            });
        }
        ps.println("end");
        ps.println();

        ps.println("return " + className);
    }

    private void genCreate(TBean tbean, TabPrintStream ps, String namespace) {
        ps.println("function " + namespace + className(tbean) + "._create(os)");
        ps.println1("local o = {}");
        tbean.fields.forEach((n, t) -> {
            Field f = tbean.define.fields.get(n);
            String c = f.desc.isEmpty() ? "" : " -- " + f.desc;

            if (t instanceof TList) {
                ps.println1("o." + lower1(n) + " = {}" + c);
                ps.println1("for _ = 1, os:ReadSize() do");
                ps.println2("table.insert(o." + lower1(n) + ", " + _create(((TList) t).value) + ")");
                ps.println1("end");
            } else if (t instanceof TMap) {
                ps.println1("o." + lower1(n) + " = {}" + c);
                ps.println1("for _ = 1, os:ReadSize() do");
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
    }

    private void genCfgs(TabPrintStream ps) throws IOException {
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

        value.type.tbeans.values().stream().filter(TBean::hasRef).forEach(t -> genResolve(t, ps));
        value.type.cfgs.values().stream().filter(c -> c.tbean.hasRef()).forEach(c -> genResolve(c.tbean, ps));
        ps.println();

        genResolveAll(ps);
        genInitializeAll(ps);
        genLoad(ps);

        ps.println("return " + pkg);
    }

    private void genResolve(TBean tbean, TabPrintStream ps) {
        String csv = "\"" + tbean.define.name + "\"";

        ps.println("local function " + resolveFuncName(tbean) + "(o, errors)");
        tbean.fields.forEach((n, t) -> {
                    if (t.hasRef()) {
                        String field = "\"" + n + "\"";
                        if (t instanceof TList) {
                            TList tt = (TList) t;
                            if (tt.value instanceof TBean && tt.value.hasRef()) {
                                ps.println1("for _, v in ipairs(o." + lower1(n) + ") do");
                                ps.println2(resolveFuncName((TBean) tt.value) + "(v, errors)");
                                ps.println1("end");
                            }
                            for (SRef sr : t.constraint.refs) {
                                ps.println1("o." + refName(sr) + " = {};");
                                ps.println1("for _, v in ipairs(o." + lower1(n) + ") do");
                                ps.println2("local r = " + fullName(sr.ref) + ".get(v)");
                                ps.println2("if r == nil then");
                                ps.println3("errors.refNil(" + csv + ", " + field + ", v)");
                                ps.println2("end");
                                ps.println2("table.insert(o." + refName(sr) + ", r)");
                                ps.println1("end");
                            }
                        } else if (t instanceof TMap) {
                            TMap tt = (TMap) t;
                            if (tt.value instanceof TBean && tt.value.hasRef()) {
                                ps.println1("for _, v in pairs(o." + lower1(n) + ") do");
                                ps.println2(resolveFuncName((TBean) tt.value) + "(v, errors)");
                                ps.println1("end");
                            }
                            t.constraint.refs.stream().filter(sr -> sr.ref != null).forEach(sr -> {
                                ps.println1("o." + refName(sr) + " = {};");
                                ps.println1("for k, v in pairs(o." + lower1(n) + ") do");
                                ps.println2("local r = " + fullName(sr.ref) + ".get(v)");
                                ps.println2("if r == nil then");
                                ps.println3("errors.refNil(" + csv + ", " + field + ", v)");
                                ps.println2("end");
                                ps.println2("o." + refName(sr) + "[k] = r");
                                ps.println1("end");
                            });
                        } else {
                            if (t instanceof TBean && t.hasRef()) {
                                ps.println1(resolveFuncName((TBean) t) + "(o." + lower1(n) + ", errors)");
                            }
                            for (SRef sr : t.constraint.refs) {
                                ps.println1("o." + refName(sr) + " = " + fullName(sr.ref) + ".get(o." + lower1(n) + ")");
                                if (!sr.nullable) {
                                    ps.println1("if o." + refName(sr) + " == nil then");
                                    ps.println2("errors.refNil(" + csv + ", " + field + ", o." + lower1(n) + ")");
                                    ps.println1("end");
                                }
                            }
                        }
                    }
                }
        );

        tbean.mRefs.forEach(m -> {
                    ps.println1("o." + refName(m) + " = " + fullName(m.ref) + ".get(" + actualParams(m.define.keys, "o.") + ");");
                    if (!m.define.nullable) {
                        ps.println1("if o." + refName(m) + " == nil then");
                        ps.println2("errors.refNil(" + csv + ", \"" + m.define.name + "\", 0)");
                        ps.println1("end");
                    }
                }
        );

        tbean.listRefs.forEach(l -> {
                    ps.println1("for _, v in pairs(" + fullName(l.ref) + ".all) do");
                    List<String> eqs = new ArrayList<>();
                    for (int i = 0; i < l.keys.length; i++) {
                        String k = l.keys[i];
                        String rk = l.refKeys[i];
                        eqs.add("v." + lower1(rk) + " == o." + lower1(k));
                    }
                    ps.println2("if " + String.join(" and ", eqs) + " then");
                    ps.println3("table.insert(o." + refName(l) + ", v)");
                    ps.println2("end");
                    ps.println1("end");
                }
        );

        ps.println("end");
        ps.println();
    }

    private void genResolveAll(TabPrintStream ps) {
        ps.println("local function _resolveAll(errors)");
        value.type.cfgs.values().stream().filter(c -> c.tbean.hasRef()).forEach(c -> {
            ps.println1("for _, v in pairs(" + fullName(c) + ".all) do");
            ps.println2(resolveFuncName(c.tbean) + "(v, errors)");
            ps.println1("end");
        });
        ps.println("end");
        ps.println();
    }

    private void genInitializeAll(TabPrintStream ps) throws IOException {
        appendFile("errors.lua", ps.ps);
        ps.println();

        ps.println("local function _CSVProcessor(os)");
        ps.println1("local cfgNils = {}");
        for (String name : value.type.cfgs.keySet()) {
            ps.println1("cfgNils[\"" + name + "\"] = 1");
        }

        ps.println1("while true do");
        ps.println2("local c = os:ReadCfg()");
        ps.println2("if c == nil then");
        ps.println3("break");
        ps.println2("end");
        ps.println2("cfgNils[c] = nil");
        ps.println2("local cc = _get(" + pkg + ", c)");
        ps.println2("if cc == nil then");
        ps.println3("errors.cfgDataAdd(c)");
        ps.println2("else");
        ps.println3("cc._initialize(os, errors)");
        ps.println2("end");
        ps.println1("end");

        ps.println1("for c, _ in pairs(cfgNils) do");
        ps.println2("errors.cfgNil(c)");
        ps.println1("end");

        ps.println1("_resolveAll(errors)");

        ps.println("end");
        ps.println();
    }

    private void genLoad(TabPrintStream ps) {
        ps.println("function " + pkg + ".Load(packDir)");
        ps.println1("Config.CSVLoader.Processor = _CSVProcessor");
        ps.println1("Config.CSVLoader.LoadPack(packDir)");
        ps.println1("return errors.errors");
        ps.println("end");
        ps.println();
    }

    private String formalParams(Map<String, Type> fs) {
        return String.join(", ", fs.keySet().stream().map(Generator::lower1).collect(Collectors.toList()));
    }

    private String actualParams(Map<String, Type> keys, String prefix) {
        return String.join(" ..\",\".. ", keys.entrySet().stream().map(e ->
                        e.getValue() instanceof TBool ? "(" + prefix + lower1(e.getKey()) + " and 1 or 0)" : prefix + lower1(e.getKey())
        ).collect(Collectors.toList()));
    }

    private String actualParams(String[] keys, String prefix) {
        return String.join(", ", Arrays.asList(keys).stream().map(n -> prefix + lower1(n)).collect(Collectors.toList()));
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

    private String className(TBean tbean) {
        return tbean.isBean ? tbean.define.name.toLowerCase() : new Name(pkg, tbean.define.name).className;
    }

    private String resolveFuncName(TBean tbean) {
        return "_resolve_" + fullName(tbean).replace(".", "_");
    }

    private String _create(Type t) {
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return "os:ReadBool()";
            }

            @Override
            public String visit(TInt type) {
                return "os:ReadInt32()";
            }

            @Override
            public String visit(TLong type) {
                return "os:ReadInt64()";
            }

            @Override
            public String visit(TFloat type) {
                return "os:ReadSingle()";
            }

            @Override
            public String visit(TString type) {
                return type.subtype == TString.Subtype.STRING ? "os:ReadString()" : "os:ReadText()";
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

    private void appendFile(String file, PrintStream ps) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/support/" + file);
             BufferedReader br = new BufferedReader(new InputStreamReader(is != null ? is : new FileInputStream("src/support/" + file), "GBK"))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                ps.println(line);
            }
        }
    }
}
