package configgen.genlua;

import configgen.define.Bean;
import configgen.gen.*;
import configgen.type.*;
import configgen.util.CachedFiles;
import configgen.util.CachedIndentPrinter;
import configgen.value.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

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
    private BriefNameFinder finder;


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
        Name.setPackageName(pkg);

        File dstDir = Paths.get(dir).resolve(pkg.replace('.', '/')).toFile();
        value = ctx.makeValue(own);

        StringBuilder fileDst = new StringBuilder(512 * 1024); //优化gc alloc
        StringBuilder cache = new StringBuilder(512 * 1024);
        StringBuilder tmp = new StringBuilder(128);
        try (CachedIndentPrinter ps = createCode(new File(dstDir, "_cfgs.lua"), encoding, fileDst, cache, tmp)) {
            generate_cfgs(ps);
        }
        if (preload) {
            try (CachedIndentPrinter ps = createCode(new File(dstDir, "_loads.lua"), encoding, fileDst, cache, tmp)) {
                generate_loads(ps);
            }
        }
        try (CachedIndentPrinter ps = createCode(new File(dstDir, "_beans.lua"), encoding, fileDst, cache, tmp)) {
            generate_beans(ps);
        }

        StringBuilder lineCache = new StringBuilder(256);
        finder = new BriefNameFinder(pkg);
        for (VTable v : value.getVTables()) {
            finder.clear();
            try (CachedIndentPrinter ps = createCode(dstDir.toPath().resolve(Name.tablePath(v.name)).toFile(), encoding, fileDst, cache, tmp)) {
                generate_table(v, ps, lineCache);
            }
        }

        CachedFiles.keepMetaAndDeleteOtherFiles(dstDir);
    }


    private void generate_cfgs(CachedIndentPrinter ps) {
        ps.println("local %s = {}", pkg);
        ps.println();

        ps.println("%s._mk = require \"common.mkcfg\"", pkg);
        if (!preload) {
            ps.println("local pre = %s._mk.pretable", pkg);
        }
        ps.println();

        Set<String> context = new HashSet<>();
        context.add(pkg);
        for (TTable c : value.getTDb().getTTables()) {
            String full = configgen.genlua.Name.fullName(c);
            definePkg(full, ps, context);
            if (preload) {
                ps.println("%s = {}", full);
            } else {
                ps.println("%s = pre(\"%s\")", full, full);
            }
            context.add(full);
        }
        ps.println();
        ps.println("return %s", pkg);
    }

    private void definePkg(String beanName, CachedIndentPrinter ps, Set<String> context) {
        List<String> seps = Arrays.asList(beanName.split("\\."));
        for (int i = 0; i < seps.size() - 1; i++) {
            String pkg = String.join(".", seps.subList(0, i + 1));
            if (context.add(pkg)) {
                ps.println(pkg + " = {}");
            }
        }
    }


    private void generate_loads(CachedIndentPrinter ps) {
        ps.println("local require = require");
        ps.println();
        for (TTable c : value.getTDb().getTTables()) {
            ps.println("require \"%s\"", configgen.genlua.Name.fullName(c));
        }
        ps.println();
    }


    private void generate_beans(CachedIndentPrinter ps) {
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
        for (TBean tbean : value.getTDb().getTBeans()) {
            String full = configgen.genlua.Name.fullName(tbean);
            definePkg(full, ps, context);
            context.add(full);

            if (tbean.getBeanDefine().type == Bean.BeanType.BaseDynamicBean) {
                ps.println("%s = {}", full);
                for (TBean actionBean : tbean.getChildDynamicBeans()) {
                    //function mkcfg.action(typeName, refs, ...)
                    String fulln = configgen.genlua.Name.fullName(actionBean);
                    definePkg(fulln, ps, context);
                    context.add(fulln);
                    ps.println("%s = action(\"%s\", %s, %s\n    )", fulln, actionBean.name, TypeStr.getLuaRefsString(actionBean), TypeStr.getLuaFieldsString(actionBean));
                }
            } else {
                //function mkcfg.bean(refs, ...)
                ps.println("%s = bean(%s, %s\n    )", full, TypeStr.getLuaRefsString(tbean), TypeStr.getLuaFieldsString(tbean));
            }
        }
        ps.println();
        ps.println("return Beans");
    }

    private void generate_table(VTable vtable, CachedIndentPrinter ps, StringBuilder lineCache) {
        TTable ttable = vtable.getTTable();
        TBean tbean = ttable.getTBean();

        ps.println("local %s = require \"%s._cfgs\"", pkg, pkg);
        if (ttable.getTBean().hasSubBean()) {
            ps.println("local Beans = %s._beans", pkg);
        }
        ps.println();


        ps.println("local this = %s", Name.fullName(vtable.getTTable()));
        ps.println();

        //function mkcfg.table(self, uniqkeys, enumidx, refs, ...)
        ps.println("local mk = %s._mk.table(this, %s, %s, %s, %s\n    )", pkg, TypeStr.getLuaUniqKeysString(ttable), TypeStr.getLuaEnumIdxString(ttable), TypeStr.getLuaRefsString(tbean), TypeStr.getLuaFieldsString(tbean));
        ps.println();


        ps.enableCache();
        for (VBean vBean : vtable.getVBeanList()) {
            lineCache.setLength(0);
            ValueStr.getLuaValueString(lineCache, vBean, finder, "mk", false);
            ps.println(lineCache.toString());
        }
        ps.disableCache();

        if (!finder.usedFullNameToBriefNames.isEmpty()) {
            for (Map.Entry<String, String> entry : finder.usedFullNameToBriefNames.entrySet()) {
                ps.println("local %s = %s", entry.getValue(), entry.getKey());
            }
            ps.println();
        }

        ps.printCache();
        ps.println();
        ps.println("return this");
    }
}
