package configgen.gen;

import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.ForeignKey;
import configgen.type.*;
import configgen.value.VDb;
import configgen.value.VTable;

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

    private final String dir;
    private final String pkg;
    private final String encoding;
    private final String own;
    private VDb value;

    public GenLua(Parameter parameter) {
        super(parameter);
        dir = parameter.get("dir", ".");
        pkg = parameter.getNotEmpty("pkg", "cfg");
        encoding = parameter.get("encoding", "UTF-8");
        own = parameter.get("own", null);
        parameter.end();
    }

    @Override
    public void generate(VDb _value) throws IOException {
        File dstDir = Paths.get(dir).resolve(pkg.replace('.', '/')).toFile();
        value = own != null ? extract(_value, own) : _value;
        try (TabPrintStream ps = createSource(new File(dstDir, "_beans.lua"), encoding)) {
            generate_beans(ps);
        }
        for (VTable c : value.vtables.values()) {
            Name name = new Name(pkg, c.name);
            try (TabPrintStream ps = createSource(dstDir.toPath().resolve(name.path).toFile(), encoding)) {
                generateTable(c.tableType, c, ps);
            }
        }
        try (TabPrintStream ps = createSource(new File(dstDir, "_cfgs.lua"), encoding)) {
            generate_cfgs(ps);
        }
        CachedFileOutputStream.keepMetaAndDeleteOtherFiles(dstDir);
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

    private void generate_beans(TabPrintStream ps) throws IOException {
        ps.println("local Beans = {}");
        Set<String> context = new HashSet<>();
        context.add("Beans");

        for (TBean tbean : value.dbType.tbeans.values()) {
            if (tbean.beanDefine.type == Bean.BeanType.BaseAction) {
                define(fullName(tbean), ps, context);
                generateBaseActionCreate(tbean, ps);
                for (TBean actionBean : tbean.actionBeans.values()) {
                    define(fullName(actionBean), ps, context);
                    generateCreateAndAssign(actionBean, ps, fullName(actionBean));
                    generateActionType(actionBean, ps, fullName(actionBean));
                }
            } else {
                define(fullName(tbean), ps, context);
                generateCreateAndAssign(tbean, ps, fullName(tbean));
            }
        }
        ps.println("return Beans");
    }


    private void generate_cfgs(TabPrintStream ps) throws IOException {
        ps.println("local " + pkg + " = {}");
        Set<String> context = new HashSet<>();
        context.add(pkg);

        for (TTable c : value.dbType.ttables.values()) {
            Name name = new Name(pkg, c.tbean.beanDefine.name);
            define(name.pkg, ps, context);
            ps.println(name.fullName + " = require(\"" + name.fullName + "\")");
            context.add(name.fullName);
        }
        ps.println();

        value.dbType.tbeans.values().stream().filter(TBean::hasRef).forEach(t -> {
            if (t.beanDefine.type == Bean.BeanType.BaseAction){
                for (TBean actionBean : t.actionBeans.values()) {
                    if (actionBean.hasRef())
                        generateResolve(actionBean, ps);
                }
            }
            generateResolve(t, ps);
        });
        value.dbType.ttables.values().stream().filter(c -> c.tbean.hasRef()).forEach(c -> generateResolve(c.tbean, ps));
        ps.println();

        generateResolveAll(ps);
        generateInitializeAll(ps);
        generateLoad(ps);

        ps.println("return " + pkg);
    }


    private void define(String beanName, TabPrintStream ps, Set<String> context) {
        List<String> seps = Arrays.asList(beanName.split("\\."));
        for (int i = 0; i < seps.size(); i++) {
            String pkg = String.join(".", seps.subList(0, i + 1));
            if (context.add(pkg)) {
                ps.println(pkg + " = {}");
            }
        }
    }

    private void generateBaseActionCreate(TBean tbean, TabPrintStream ps) {
        ps.println("function " + fullName(tbean) + ":_create(os)");
        ps.println1("local s = os:ReadString()");
        boolean first = true;
        for (TBean actionBean : tbean.actionBeans.values()) {
            ps.println1((first ? "if" : "elseif") + " s == '" + actionBean.name + "' then");
            ps.println2("return " + fullName(actionBean) + ":_create(os)");
            first = false;
        }
        ps.println1("end");
        ps.println("end");
    }

    private void generateCreateAndAssign(TBean tbean, TabPrintStream ps, String fullName) {
        ps.println("function " + fullName + ":_create(os)");
        ps.println1("local o = {}");
        ps.println1("setmetatable(o, self)");
        ps.println1("self.__index = self");
        tbean.columns.forEach((n, t) -> {
            Column f = tbean.beanDefine.columns.get(n);
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

            t.constraint.references.forEach(r -> ps.println1("o." + refName(r) + " = nil"));
        });
        tbean.mRefs.forEach(m -> ps.println1("o." + refName(m) + " = nil"));
        tbean.listRefs.forEach(l -> ps.println1("o." + refName(l) + " = {}"));
        ps.println1("return o");
        ps.println("end");
        ps.println();

        //_assign
        ps.println("function " + fullName + ":_assign(other)");
        tbean.columns.forEach((n, t) -> {
            Column f = tbean.beanDefine.columns.get(n);

            if (t instanceof TBean) {
                ps.println1("self." + lower1(n) + ":_assign(other." + lower1(n) + ")");
            } else if (t instanceof TList || t instanceof TMap) {
                ps.println1("for k, v in pairs(other." + lower1(n) + ") do");
                ps.println2("self." + lower1(n) + "[k] = v");
                ps.println1("end");
            } else {
                ps.println1("self." + lower1(n) + " = other." + lower1(n));
            }
        });
        ps.println("end");
        ps.println();
    }

    private void generateActionType(TBean actionBean, TabPrintStream ps, String fullName) {
        ps.println("function " + fullName + ":type()");
        ps.println1("return '" + actionBean.name + "'");
        ps.println("end");
        ps.println();
    }

    private void generateTable(TTable ttable, VTable vtable, TabPrintStream ps) {
        String className = new Name(pkg, ttable.name).className;
        if (ttable.tbean.hasSubBean()) {
            ps.println("local Beans = require(\"" + pkg + "._beans\")");
            ps.println();
        }

        ps.println("local " + className + " = {}");
        vtable.enumNames.forEach(e -> ps.println(className + "." + e + " = nil"));
        ps.println();
        generateCreateAndAssign(ttable.tbean, ps, className);

        //static get
        generateMapGetBy(ttable.primaryKey, className, ps, true);
        for (Map<String, Type> uniqueKey : ttable.uniqueKeys) {
            generateMapGetBy(uniqueKey, className, ps, false);
        }

        //static _initialize
        ps.println("function " + className + "._initialize(os, errors)");
        ps.println1("for _ = 1, os:ReadSize() do");
        ps.println2("local v = " + className + ":_create(os)");
        if (vtable.isEnum) {
            ps.println2("if #(v." + lower1(ttable.tableDefine.enumStr) + ") > 0 then");
            ps.println3(className + "[v." + lower1(ttable.tableDefine.enumStr) + "] = v");
            ps.println2("end");
        }

        generateAllMapPut(ttable, className, ps);

        ps.println1("end");
        if (vtable.isEnum) {
            vtable.enumNames.forEach(e -> {
                ps.println1("if " + className + "." + e + " == nil then");
                ps.println2("errors.enumNil(\"" + ttable.tbean.beanDefine.name + "\", \"" + e + "\");");
                ps.println1("end");
            });
        }
        ps.println("end");
        ps.println();

        //static _reload
        ps.println("function " + className + "._reload(os, errors)");
        ps.println1("local old = " + className + ".all");
        ps.println1(className + ".all = {}");
        ps.println1(className + "._initialize(os, errors)");
        ps.println1("for k, v in pairs(" + className + ".all) do");
        ps.println2("local ov = old[k]");
        ps.println2("if ov then");
        ps.println3("ov:_assign(v)");
        ps.println2("end");
        ps.println1("end");
        ps.println("end");
        ps.println();

        ps.println("return " + className);
    }

    private void generateMapGetBy(Map<String, Type> keys, String className, TabPrintStream ps, boolean isPrimaryKey) {
        String mapName = isPrimaryKey ? "all" : uniqueKeyMapName(keys);
        ps.println(className + "." + mapName + " = {}");
        String getByName = isPrimaryKey ? "get" : uniqueKeyGetByName(keys);
        ps.println("function " + className + "." + getByName + "(" + formalParams(keys) + ")");
        ps.println1("return " + className + "." + mapName + "[" + actualParams(keys, "") + "]");
        ps.println("end");
        ps.println();
    }

    private String uniqueKeyGetByName(Map<String, Type> keys) {
        return "getBy" + keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b);
    }

    private String uniqueKeyMapName(Map<String, Type> keys) {
        return keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b) + "Map";
    }

    private void generateAllMapPut(TTable ttable, String className, TabPrintStream ps) {
        generateMapPut(ttable.primaryKey, className, ps, true);
        for (Map<String, Type> uniqueKey : ttable.uniqueKeys) {
            generateMapPut(uniqueKey, className, ps, false);
        }
    }

    private void generateMapPut(Map<String, Type> keys, String className, TabPrintStream ps, boolean isPrimaryKey) {
        String mapName = isPrimaryKey ? "all" : uniqueKeyMapName(keys);
        ps.println2(className + "." + mapName + "[" + actualParams(keys, "v.") + "] = v");
    }

    private void generateResolve(TBean tbean, TabPrintStream ps) {
        String csv = "\"" + tbean.beanDefine.name + "\"";

        ps.println("local function " + resolveFuncName(tbean) + "(o, errors)");
        if (tbean.beanDefine.type == Bean.BeanType.BaseAction){
            boolean first = true;
            for (TBean actionBean : tbean.actionBeans.values()) {
                if (actionBean.hasRef()){
                    ps.println1((first ? "if" : "elseif") + " o:type() == '" + actionBean.name + "' then");
                    ps.println2(resolveFuncName(actionBean) + "(o, errors)");
                    first = false;
                }
            }
            ps.println1("end");
        }
        tbean.columns.forEach((n, t) -> {
                    if (t.hasRef()) {
                        String field = "\"" + n + "\"";
                        if (t instanceof TList) {
                            TList tt = (TList) t;
                            if (tt.value instanceof TBean && tt.value.hasRef()) {
                                ps.println1("for _, v in ipairs(o." + lower1(n) + ") do");
                                ps.println2(resolveFuncName((TBean) tt.value) + "(v, errors)");
                                ps.println1("end");
                            }
                            for (SRef sr : t.constraint.references) {
                                ps.println1("o." + refName(sr) + " = {}");
                                ps.println1("for _, v in ipairs(o." + lower1(n) + ") do");
                                ps.println2("local r = " + fullName(sr.refTable) + ".get(v)");
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
                            t.constraint.references.stream().filter(sr -> sr.refTable != null).forEach(sr -> {
                                ps.println1("o." + refName(sr) + " = {}");
                                ps.println1("for k, v in pairs(o." + lower1(n) + ") do");
                                ps.println2("local r = " + fullName(sr.refTable) + ".get(v)");
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
                            for (SRef sr : t.constraint.references) {
                                ps.println1("o." + refName(sr) + " = " + fullName(sr.refTable) + ".get(o." + lower1(n) + ")");
                                if (!sr.refNullable) {
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
                    ps.println1("o." + refName(m) + " = " + fullName(m.refTable) + ".get(" + actualParams(m.foreignKeyDefine.keys, "o.") + ");");
                    if (m.foreignKeyDefine.refType != ForeignKey.RefType.NULLABLE) {
                        ps.println1("if o." + refName(m) + " == nil then");
                        ps.println2("errors.refNil(" + csv + ", \"" + m.foreignKeyDefine.name + "\", 0)");
                        ps.println1("end");
                    }
                }
        );

        tbean.listRefs.forEach(l -> {
                    ps.println1("for _, v in pairs(" + fullName(l.refTable) + ".all) do");
                    List<String> eqs = new ArrayList<>();
                    for (int i = 0; i < l.foreignKeyDefine.keys.length; i++) {
                        String k = l.foreignKeyDefine.keys[i];
                        String rk = l.foreignKeyDefine.ref.cols[i];
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

    private void generateResolveAll(TabPrintStream ps) {
        ps.println("local function _resolveAll(errors)");
        value.dbType.ttables.values().stream().filter(c -> c.tbean.hasRef()).forEach(c -> {
            ps.println1("for _, v in pairs(" + fullName(c) + ".all) do");
            ps.println2(resolveFuncName(c.tbean) + "(v, errors)");
            ps.println1("end");
        });
        ps.println("end");
        ps.println();
    }

    private void generateInitializeAll(TabPrintStream ps) throws IOException {
        appendFile("errors.lua", ps.ps);
        ps.println();

        ps.println("local _reload = false");
        ps.println("local function _CSVProcessor(os)");
        ps.println1("local cfgNils = {}");
        for (String name : value.dbType.ttables.keySet()) {
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
        ps.println2("elseif _reload then");
        ps.println3("cc._reload(os, errors)");
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

    private void generateLoad(TabPrintStream ps) {
        ps.println("function " + pkg + ".Load(packDir, reload)");
        ps.println1("_reload = reload");
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
        if (tbean.beanDefine.type == Bean.BeanType.Table)
            return new Name(pkg, tbean.name).fullName;
        else if (tbean.beanDefine.type == Bean.BeanType.Action)
            return "Beans." + (((TBean) tbean.parent)).name.toLowerCase() + "." + tbean.name.toLowerCase();
        else
            return "Beans." + tbean.name.toLowerCase();
    }

    private String fullName(TTable ttable) {
        return fullName(ttable.tbean);
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
                return fullName(type) + ":_create(os)";
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
