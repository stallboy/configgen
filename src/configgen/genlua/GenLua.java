package configgen.genlua;

import configgen.define.Bean;
import configgen.define.Column;
import configgen.gen.*;
import configgen.util.IndentPrint;
import configgen.type.*;
import configgen.util.CachedFileOutputStream;
import configgen.value.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class GenLua extends Generator {

    public static void register() {
        Generators.addProvider("lua", new GeneratorProvider() {
            @Override
            public Generator create(Parameter parameter) {
                return new GenLua(parameter);
            }

            @Override
            public String usage() {
                return "dir:.,pkg:cfg,encoding:UTF-8,preload:false    add own:x if need";
            }
        });
    }

    private final String dir;
    private final String pkg;
    private final String encoding;
    private final String own;
    private final boolean preload;
    private VDb value;

    private GenLua(Parameter parameter) {
        super(parameter);
        dir = parameter.get("dir", ".");
        pkg = parameter.getNotEmpty("pkg", "cfg");
        encoding = parameter.get("encoding", "UTF-8");
        own = parameter.get("own", null);
        preload = Boolean.parseBoolean(parameter.get("preload", "false"));
        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        File dstDir = Paths.get(dir).resolve(pkg.replace('.', '/')).toFile();
        value = ctx.makeValue(own);

        try (IndentPrint ps = createCode(new File(dstDir, "_cfgs.lua"), encoding)) {
            generate_cfgs(ps);
        }
        if (preload){
            try (IndentPrint ps = createCode(new File(dstDir, "_loads.lua"), encoding)) {
                generate_loads(ps);
            }
        }
        try (IndentPrint ps = createCode(new File(dstDir, "_beans.lua"), encoding)) {
            generate_beans(ps);
        }

        for (VTable v : value.getVTables()) {
            Name name = new Name(pkg, v.name);
            try (IndentPrint ps = createCode(dstDir.toPath().resolve(name.path).toFile(), encoding)) {
                generate_table(v, name, ps);
            }
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


    private void generate_cfgs(IndentPrint ps) {
        ps.println("local %s = {}", pkg);
        ps.println();

        ps.println("%s._mk = require \"common.mkcfg\"", pkg);
        if (!preload) {
            ps.println("local pre = %s._mk.pretable", pkg);
        }
        ps.println();

        Set<String> context = new HashSet<>();
        context.add(pkg);
        for (TTable c : value.getDbType().ttables.values()) {
            String full = fullName(c);
            definePkg(full, ps, context);
            if (preload){
                ps.println("%s = {}", full);
            }else{
                ps.println("%s = pre(\"%s\")", full, full);
            }
            context.add(full);
        }
        ps.println();
        ps.println("return %s", pkg);
    }

    private void definePkg(String beanName, IndentPrint ps, Set<String> context) {
        List<String> seps = Arrays.asList(beanName.split("\\."));
        for (int i = 0; i < seps.size() - 1; i++) {
            String pkg = String.join(".", seps.subList(0, i + 1));
            if (context.add(pkg)) {
                ps.println(pkg + " = {}");
            }
        }
    }


    private void generate_loads(IndentPrint ps) {
        ps.println("local require = require");
        ps.println();
        for (TTable c : value.getDbType().ttables.values()) {
            ps.println("require \"%s\"", fullName(c));
        }
        ps.println();
    }


    private void generate_beans(IndentPrint ps) {
        ps.println("local %s = require \"%s._cfgs\"", pkg, pkg);
        ps.println();

        ps.println("local Beans = {}");
        ps.println("%s._beans = Beans", pkg);
        ps.println();
        ps.println("local bean = %s._mk.bean", pkg);
        ps.println("local action = %s._mk.action", pkg);
        ps.println();

        Set<String> context = new HashSet<>();
        context.add("Beans");
        for (TBean tbean : value.getDbType().tbeans.values()) {
            String full = fullName(tbean);
            definePkg(full, ps, context);
            context.add(full);

            if (tbean.beanDefine.type == Bean.BeanType.BaseAction) {
                ps.println("%s = {}", full);
                for (TBean actionBean : tbean.actionBeans.values()) {
                    //function mkcfg.action(typeName, refs, ...)
                    String fulln = fullName(actionBean);
                    definePkg(fulln, ps, context);
                    context.add(fulln);
                    ps.println("%s = action(\"%s\", %s, %s\n    )", fulln, actionBean.name, getLuaRefsString(actionBean), getLuaFieldsString(actionBean));
                }
            } else {
                //function mkcfg.bean(refs, ...)
                ps.println("%s = bean(%s, %s\n    )", full, getLuaRefsString(tbean), getLuaFieldsString(tbean));
            }
        }
        ps.println();
        ps.println("return Beans");
    }

    private void generate_table(VTable vtable, Name name, IndentPrint ps) {
        TTable ttable = vtable.tableType;
        TBean tbean = ttable.tbean;

        ps.println("local %s = require \"%s._cfgs\"", pkg, pkg);
        if (ttable.tbean.hasSubBean()) {
            ps.println("local Beans = %s._beans", pkg);
        }
        ps.println();


        ps.println("local this = %s", name.fullName);
        ps.println();

        //function mkcfg.table(self, uniqkeys, enumidx, refs, ...)
        ps.println("local mk = %s._mk.table(this, %s, %s, %s, %s\n    )", pkg, getLuaUniqKeysString(ttable), getLuaEnumIdxString(ttable), getLuaRefsString(tbean), getLuaFieldsString(tbean));
        ps.println();

        for (VBean vBean : vtable.getVBeanList()) {
            ps.println(getLuaValueString(vBean, "mk", false));
        }

        ps.println();
        ps.println("return this");
    }

    private String getLuaValueString(Value thisValue) {
        return getLuaValueString(thisValue, null, false);
    }

    private String getLuaValueString(Value thisValue, String beanTypeStr, boolean asKey) {
        String[] v = new String[1];
        thisValue.accept(new ValueVisitor() {
            @Override
            public void visit(VBool value) {
                String val = value.value ? "true" : "false";
                if (asKey) {
                    v[0] = String.format("[%s]", val);
                } else {
                    v[0] = val;
                }
            }

            @Override
            public void visit(VInt value) {
                if (asKey) {
                    v[0] = String.format("[%s]", String.valueOf(value.value));
                } else {
                    v[0] = String.valueOf(value.value);
                }
            }

            @Override
            public void visit(VLong value) {
                if (asKey) {
                    v[0] = String.format("[%s]", String.valueOf(value.value));
                } else {
                    v[0] = String.valueOf(value.value);
                }
            }

            @Override
            public void visit(VFloat value) {
                if (asKey) {
                    v[0] = String.format("[%s]", String.valueOf(value.value));
                } else {
                    v[0] = String.valueOf(value.value);
                }
            }

            @Override
            public void visit(VString value) {
                String val = value.value.replace("\r\n", "\\n");
                val = val.replace("\n", "\\n");
                String val2 = val.replace("\"", "\\\"");
                if (asKey) {
                    if (keywords.contains(val2) || val2.contains("-") || val2.contains("=") || val2.contains(",")) {
                        v[0] = String.format("[\"%s\"]", val2);
                    } else {
                        v[0] = val2;
                    }
                } else {
                    v[0] = String.format("\"%s\"", val2);
                }
            }

            @Override
            public void visit(VList value) {
                List<String> list = new ArrayList<>();
                for (Value eleValue : value.getList()) {
                    list.add(getLuaValueString(eleValue));
                }
                String liststr = String.join(", ", list);
                v[0] = String.format("{%s}", liststr);
            }

            @Override
            public void visit(VMap value) {
                List<String> list = new ArrayList<>();
                for (Map.Entry<Value, Value> entry : value.map.entrySet()) {
                    String key = getLuaValueString(entry.getKey(), null, true);
                    String val = getLuaValueString(entry.getValue());
                    list.add(String.format("%s = %s", key, val));
                }
                String liststr = String.join(", ", list);
                v[0] = String.format("{%s}", liststr);
            }

            @Override
            public void visit(VBean value) {
                VBean val = value;
                if (value.beanType.beanDefine.type == Bean.BeanType.BaseAction) {
                    val = value.actionVBean;
                }
                String beanType = beanTypeStr;
                if (beanType == null) {
                    beanType = fullName(val.beanType);
                }

                List<String> list = new ArrayList<>();
                for (Value fieldValue : val.getValues()) {
                    list.add(getLuaValueString(fieldValue));
                }
                String params = String.join(", ", list);
                v[0] = String.format("%s(%s)", beanType, params);
            }
        });

        return v[0];
    }

    private static Set<String> keywords = new HashSet<>(Arrays.asList("break", "goto", "do", "end", "for", "in", "repeat", "util", "while", "if", "then", "elseif", "function", "local", "nil", "true", "false"));

    // uniqkeys : {{allname=, getname=, keyidx1=, keyidx2=}, }
    private String getLuaUniqKeysString(TTable ttable) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        sb.append(getLuaOneUniqKeyString(ttable, ttable.primaryKey, true));
        for (Map<String, Type> uniqueKey : ttable.uniqueKeys) {
            sb.append(getLuaOneUniqKeyString(ttable, uniqueKey, false));
        }
        sb.append("}");
        return sb.toString();
    }

    private String getLuaOneUniqKeyString(TTable ttable, Map<String, Type> keys, boolean isPrimaryKey) {
        String allname = isPrimaryKey ? "all" : uniqueKeyMapName(keys);
        String getname = isPrimaryKey ? "get" : uniqueKeyGetByName(keys);

        Iterator<String> it = keys.keySet().iterator();
        String key1 = it.next();
        int keyidx1 = findFieldIdx(ttable.tbean, key1);

        boolean hasKeyIdx2 = false;
        int keyidx2 = 0;
        if (keys.size() > 1) {
            if (keys.size() != 2) {
                throw new RuntimeException("uniqkeys size != 2 " + ttable.name);
            }
            String key2 = it.next();
            hasKeyIdx2 = true;
            keyidx2 = findFieldIdx(ttable.tbean, key2);
        }

        if (hasKeyIdx2) {
            return String.format("{ \"%s\", \"%s\", %d, %d }, ", allname, getname, keyidx1, keyidx2);
        } else {
            return String.format("{ \"%s\", \"%s\", %d }, ", allname, getname, keyidx1);
        }
    }

    private String getLuaEnumIdxString(TTable ttable) {
        switch (ttable.tableDefine.enumType) {
            case None:
                return "nil";
            default:
                return String.valueOf(findFieldIdx(ttable.tbean, ttable.tableDefine.enumStr));
        }
    }

    // refs { {refname, islist, dsttable, dstgetname, keyidx1, keyidx2}, }
    private String getLuaRefsString(TBean tbean) {
        StringBuilder sb = new StringBuilder();
        boolean hasRef = false;
        sb.append("{ ");
        int i = 0;
        for (Type t : tbean.columns.values()) {
            i++;
            for (SRef r : t.constraint.references) {
                if (t instanceof TMap) {
                    System.out.println("map sref not suppport, bean=" + tbean.name);
                    break;
                }
                String refname = refName(r);
                String dsttable = fullName(r.refTable);
                String dstgetname = uniqueKeyGetByName(r.refCols);
                String islist = "false";
                if (t instanceof TList) {
                    islist = "true";
                }
                sb.append(String.format("\n    { \"%s\", %s, %s, \"%s\", %d }, ", refname, islist, dsttable, dstgetname, i));
                hasRef = true;
            }
        }

        for (TForeignKey mRef : tbean.mRefs) {
            String refname = refName(mRef);
            String dsttable = fullName(mRef.refTable);
            String dstgetname = uniqueKeyGetByName(mRef.foreignKeyDefine.ref.cols);
            int keyidx1 = findFieldIdx(tbean, mRef.foreignKeyDefine.keys[0]);

            boolean hasKeyIdx2 = false;
            int keyidx2 = 0;
            if (mRef.foreignKeyDefine.keys.length > 1) {
                if (mRef.foreignKeyDefine.keys.length != 2) {
                    throw new RuntimeException("keys length != 2 " + tbean.name);
                }
                hasKeyIdx2 = true;
                keyidx2 = findFieldIdx(tbean, mRef.foreignKeyDefine.keys[1]);
            }
            if (hasKeyIdx2) {
                sb.append(String.format("\n    { \"%s\", false, %s, \"%s\", %d, %d }, ", refname, dsttable, dstgetname, keyidx1, keyidx2));
            } else {
                sb.append(String.format("\n    { \"%s\", false, %s, \"%s\", %d }, ", refname, dsttable, dstgetname, keyidx1));
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
            } else {
                sb.append("\n    \"").append(lower1(n)).append("\"  -- ").append(f.type).append(c);
            }
        }

        return sb.toString();
    }

    private int findFieldIdx(TBean tbean, String fieldName) {
        int i = 0;
        for (String s : tbean.columns.keySet()) {
            i++;
            if (s.equals(fieldName)) {
                return i;
            }
        }
        throw new RuntimeException("key idx not found " + tbean.name + ", " + fieldName);
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

}
