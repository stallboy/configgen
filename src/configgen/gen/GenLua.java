package configgen.gen;

import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.ForeignKey;
import configgen.genjava.IndentPrint;
import configgen.type.*;
import configgen.value.VDb;
import configgen.value.VTable;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        try (IndentPrint ps = createCode(new File(dstDir, "_beans.lua"), encoding)) {
            generate_beans(ps);
        }
        try (IndentPrint ps = createCode(new File(dstDir, "_cfgs.lua"), encoding)) {
            generate_cfgs(ps);
        }
        try (IndentPrint ps = createCode(new File(dstDir, "_loads.lua"), encoding)) {
            generate_loads(ps);
        }

        for (VTable c : value.vtables.values()) {
            Name name = new Name(pkg, c.name);
            //try (IndentPrint ps = createCode(dstDir.toPath().resolve(name.path).toFile(), encoding)) {
                //generateTable(c.tableType, c, ps);
            //}
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

    private void generate_beans(IndentPrint ps) {
        ps.println("local %s = require \"%s._cfgs\"", pkg, pkg);
        ps.println();

        ps.println("local Beans = {}");
        ps.println("%s._beans = Beans", pkg);
        ps.println();
        ps.println("local bean = %s._mk.bean", pkg);
        ps.println("local tbean = %s._mk.tbean", pkg);
        ps.println();

        for (TBean tbean : value.dbType.tbeans.values()) {
            if (tbean.beanDefine.type == Bean.BeanType.BaseAction) {
                ps.println("%s = {}", fullName(tbean));
                for (TBean actionBean : tbean.actionBeans.values()) {
                    ps.println("%s = tbean(\"%s\", %s, %s\n    )", fullName(actionBean), actionBean.name, getLuaRefsString(actionBean), getLuaFieldsString(actionBean));
                }
            } else {
                ps.println("%s = bean(%s, %s\n    )", fullName(tbean), getLuaRefsString(tbean), getLuaFieldsString(tbean));
            }
        }
        ps.println();
        ps.println("return Beans");
    }


    private void generate_cfgs(IndentPrint ps) {
        ps.println("local %s = {}", pkg);
        ps.println("%s._mk = require \"common.mkcfg\"", pkg);

        Set<String> context = new HashSet<>();
        context.add(pkg);
        for (TTable c : value.dbType.ttables.values()) {
            String name = fullName(c);
            define(name, ps, context);
            context.add(name);
        }
        ps.println();
        ps.println("return %s", pkg);
    }


    private void generate_loads(IndentPrint ps) {
        ps.println("local require = require");
        ps.println();
        for (TTable c : value.dbType.ttables.values()) {
            ps.println("require \"%s\"", fullName(c));
        }
        ps.println();
    }

    private void define(String beanName, IndentPrint ps, Set<String> context) {
        List<String> seps = Arrays.asList(beanName.split("\\."));
        for (int i = 0; i < seps.size(); i++) {
            String pkg = String.join(".", seps.subList(0, i + 1));
            if (context.add(pkg)) {
                ps.println(pkg + " = {}");
            }
        }
    }


    // uniqkeys : {{allname=, getname=, keyidx1=, keyidx2=}, }
    private String getLuaUniqKeysString(TTable ttable) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        sb.append(getOneUniqKeyString(ttable, ttable.primaryKey, true));
        for (Map<String, Type> uniqueKey : ttable.uniqueKeys) {
            sb.append(getOneUniqKeyString(ttable, uniqueKey, false));
        }
        sb.append("}");
        return sb.toString();
    }

    private String getOneUniqKeyString(TTable ttable, Map<String, Type> keys, boolean isPrimaryKey) {
        String allname = isPrimaryKey ? "all" : uniqueKeyMapName(keys);
        String getname = isPrimaryKey ? "get" : uniqueKeyGetByName(keys);

        Iterator<String> it = keys.keySet().iterator();
        String key1 = it.next();
        int keyidx1 = getKeyIdx(ttable.tbean, key1);

        boolean hasKeyIdx2 = false;
        int keyidx2 = 0;
        if (keys.size() > 1) {
            if (keys.size() != 2) {
                throw new RuntimeException("uniqkeys size != 2 " + ttable.name);
            }
            String key2 = it.next();
            hasKeyIdx2 = true;
            keyidx2 = getKeyIdx(ttable.tbean, key2);
        }

        if (hasKeyIdx2) {
            return String.format("{ allname = \"%s\", getname = \"%s\", keyidx1 = %d, keyidx2 = %d }, ", allname, getname, keyidx1, keyidx2);
        } else {
            return String.format("{ allname = \"%s\", getname = \"%s\", keyidx1 = %d }, ", allname, getname, keyidx1);
        }
    }

    private String uniqueKeyGetByName(Map<String, Type> keys) {
        return "getBy" + keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b);
    }

    private String uniqueKeyMapName(Map<String, Type> keys) {
        return keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b) + "Map";
    }

    private String uniqueKeyGetByName(String[] cols) {
        if (cols.length == 0) //ref to primary key
            return "get";
        else
            return "getBy" + Stream.of(cols).map(Generator::upper1).reduce("", (a, b) -> a + b);
    }

    // refs { {refname=, dsttable=, dstgetname=, keyidx1=, keyidx2=}, }
    private String getLuaRefsString(TBean tbean) {
        StringBuilder sb = new StringBuilder();
        boolean hasRef = false;
        sb.append("{ ");
        int i = 0;
        for (Type t : tbean.columns.values()) {
            i++;
            for (SRef r : t.constraint.references) {
                String refname = refName(r);
                String dsttable = fullName(r.refTable);
                String dstgetname = uniqueKeyGetByName(r.refCols);
                sb.append(String.format("{ refname = \"%s\", dsttable = %s, dstgetname = \"%s\", keyidx1 = %d }, ", refname, dsttable, dstgetname, i));
                hasRef = true;
            }
        }

        for (TForeignKey mRef : tbean.mRefs) {
            String refname = refName(mRef);
            String dsttable = fullName(mRef.refTable);
            String dstgetname = uniqueKeyGetByName(mRef.foreignKeyDefine.ref.cols);
            int keyidx1 = getKeyIdx(tbean, mRef.foreignKeyDefine.keys[0]);

            boolean hasKeyIdx2 = false;
            int keyidx2 = 0;
            if (mRef.foreignKeyDefine.keys.length > 1) {
                if (mRef.foreignKeyDefine.keys.length != 2) {
                    throw new RuntimeException("keys length != 2 " + tbean.name);
                }
                hasKeyIdx2 = true;
                keyidx2 = getKeyIdx(tbean, mRef.foreignKeyDefine.keys[1]);
            }
            if (hasKeyIdx2) {
                sb.append(String.format("{ refname = \"%s\", dsttable = %s, dstgetname = \"%s\", keyidx1 = %d, keyidx2 = %d }, ", refname, dsttable, dstgetname, keyidx1, keyidx2));
            } else {
                sb.append(String.format("{ refname = \"%s\", dsttable = %s, dstgetname = \"%s\", keyidx1 = %d }, ", refname, dsttable, dstgetname, keyidx1));
            }
            hasRef = true;
        }
        sb.append("}");
        //忽略ListRef

        if (hasRef) {
            return sb.toString();
        } else {
            return "nil";
        }
    }

    private int getKeyIdx(TBean tbean, String fieldName) {
        int i = 0;
        for (String s : tbean.columns.keySet()) {
            i++;
            if (s.equals(fieldName)) {
                return i;
            }
        }
        throw new RuntimeException("key idx not found " + tbean.name + ", " + fieldName);
    }

    private String getLuaFieldsString(TBean tbean) {
        StringBuilder sb = new StringBuilder();

        int cnt = tbean.columns.size();
        int i = 0;
        for (String n : tbean.columns.keySet()) {
            i++;
            Column f = tbean.beanDefine.columns.get(n);
            String c = f.desc.isEmpty() ? "" : ", " + f.desc;
            if (i < cnt) {
                sb.append("\n    \"").append(lower1(n)).append("\", -- ").append(f.type).append(c);
            }else{
                sb.append("\n    \"").append(lower1(n)).append("\"  -- ").append(f.type).append(c);
            }
        }

        return sb.toString();
    }


    private void generateMapGetBy(Map<String, Type> keys, String className, IndentPrint ps, boolean isPrimaryKey) {
        String mapName = isPrimaryKey ? "all" : uniqueKeyMapName(keys);
        ps.println(className + "." + mapName + " = {}");
        String getByName = isPrimaryKey ? "get" : uniqueKeyGetByName(keys);
        ps.println("function " + className + "." + getByName + "(" + formalParams(keys) + ")");
        ps.println1("return " + className + "." + mapName + "[" + actualParams(keys, "") + "]");
        ps.println("end");
        ps.println();
    }


    private void generateCreateAndAssign(TBean tbean, IndentPrint ps, String fullName) {
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


        ps.println();
    }

    private void generateTable(TTable ttable, VTable vtable, IndentPrint ps) {
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
        if (ttable.tableDefine.isEnum()) {
            ps.println2("if #(v." + lower1(ttable.tableDefine.enumStr) + ") > 0 then");
            ps.println3(className + "[v." + lower1(ttable.tableDefine.enumStr) + "] = v");
            ps.println2("end");
        }

        generateAllMapPut(ttable, className, ps);

        ps.println1("end");
        if (ttable.tableDefine.isEnum()) {
            vtable.enumNames.forEach(e -> {
                ps.println1("if " + className + "." + e + " == nil then");
                ps.println2("errors.enumNil(\"" + ttable.tbean.beanDefine.name + "\", \"" + e + "\");");
                ps.println1("end");
            });
        }
        ps.println("end");
        ps.println();


        ps.println("return " + className);
    }

    private void generateAllMapPut(TTable ttable, String className, IndentPrint ps) {
        generateMapPut(ttable.primaryKey, className, ps, true);
        for (Map<String, Type> uniqueKey : ttable.uniqueKeys) {
            generateMapPut(uniqueKey, className, ps, false);
        }
    }

    private void generateMapPut(Map<String, Type> keys, String className, IndentPrint ps, boolean isPrimaryKey) {
        String mapName = isPrimaryKey ? "all" : uniqueKeyMapName(keys);
        ps.println2(className + "." + mapName + "[" + actualParams(keys, "v.") + "] = v");
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
}
