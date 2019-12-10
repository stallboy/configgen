package configgen.genlua;

import configgen.Logger;
import configgen.define.Bean;
import configgen.gen.*;
import configgen.type.TBean;
import configgen.type.TTable;
import configgen.util.CachedFiles;
import configgen.util.CachedIndentPrinter;
import configgen.value.VBean;
import configgen.value.AllValue;
import configgen.value.VTable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;

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
                return "dir:.,pkg:cfg,encoding:UTF-8,preload:false,sharedemptytable:false   add own:x if need  add emmylua:true if need ";
            }
        });
    }

    private final String dir;
    private final String pkg;
    private final String encoding;
    private final String own;
    private final boolean useEmmyLua;
    private final boolean preload;
    private final boolean sharedEmptyTable;
    private AllValue value;
    private FullToBrief toBrief;

    private boolean isLangSwitch;
    private LangSwitch langSwitch;

    private GenLua(Parameter parameter) {
        super(parameter);
        dir = parameter.get("dir", ".");
        pkg = parameter.getNotEmpty("pkg", "cfg");
        encoding = parameter.get("encoding", "UTF-8");
        own = parameter.get("own", null);
        //是否生成EmmyLua相关的注解
        useEmmyLua = Boolean.parseBoolean(parameter.get("emmylua", "false"));
        // 默认是不一开始就全部加载配置，而是用到的时候再加载
        preload = Boolean.parseBoolean(parameter.get("preload", "false"));

        sharedEmptyTable = Boolean.parseBoolean(parameter.get("sharedemptytable", "false"));

        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        Name.setPackageName(pkg);
        toBrief = new FullToBrief(pkg, sharedEmptyTable);
        ValueStr.setToBrief(toBrief);
        langSwitch = ctx.getLangSwitch();
        isLangSwitch = langSwitch != null;
        ValueStr.setLangSwitch(langSwitch);

        Path dstDirPath = Paths.get(dir).resolve(pkg.replace('.', '/'));
        File dstDir = dstDirPath.toFile();
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

        for (VTable v : value.getVTables()) {
            toBrief.clear();
            try (CachedIndentPrinter ps = createCode(new File(dstDir, Name.tablePath(v.name)), encoding, fileDst, cache, tmp)) {
                generate_table(v, ps, lineCache);
            }
        }

        Logger.log(String.format("共享的空table个数:%d", toBrief.getAllEmptyTableUsedCount()));

        if (ctx.getLangSwitch() != null) {
            for (LangSwitch.Lang lang : ctx.getLangSwitch().getAllLangInfo()) {
                List<String> idToStr = lang.getStrList();
                try (CachedIndentPrinter ps = createCode(new File(dstDir, lang.getLang() + ".lua"), encoding, fileDst, cache, tmp)) {
                    generate_lang(ps, idToStr, lineCache);
                }
            }
            copyFile(dstDirPath, "mkcfg.lua", encoding);
        }


        CachedFiles.keepMetaAndDeleteOtherFiles(dstDir);
    }

    private void generate_lang(CachedIndentPrinter ps, List<String> idToStr, StringBuilder lineCache) {
        ps.println("return {");
        for (String str : idToStr) {
            lineCache.setLength(0);
            ValueStr.getLuaString(lineCache, str);
            ps.println1(lineCache + ",");
        }
        ps.println("}");
    }


    private void generate_cfgs(CachedIndentPrinter ps) {
        ps.println("local %s = {}", pkg);
        ps.println();

        String mkcfgFrom = "common";
        if (isLangSwitch) {
            mkcfgFrom = pkg;
        }
        ps.println("%s._mk = require \"%s.mkcfg\"", pkg, mkcfgFrom);
        if (!preload) {
            ps.println("local pre = %s._mk.pretable", pkg);
        }
        ps.println();

        if (isLangSwitch) {
            ps.println("%s._last_lang = nil", pkg);
            ps.println("function %s._set_lang(lang)", pkg);
            ps.println1("if %s._last_lang == lang then", pkg);
            ps.println2("return");
            ps.println1("end");

            ps.println1("if %s._last_lang then", pkg);
            ps.println2("package.loaded[\"%s.\" .. %s._last_lang] = nil", pkg, pkg);
            ps.println1("end");

            ps.println1("%s._last_lang = lang", pkg);
            ps.println1("%s._mk.i18n = require(\"%s.\" .. lang)", pkg, pkg);
            ps.println("end");
            ps.println();
        }

        Set<String> context = new HashSet<>();
        context.add(pkg);
        for (TTable c : value.getTDb().getTTables()) {
            String full = Name.fullName(c);
            definePkg(full, ps, context);
            if (preload) {
                ps.println("%s = {}", full);
            } else {
                ps.println("%s = pre(\"%s\")", full, full);
            }
            context.add(full);
        }

        if (useEmmyLua) {
            context.clear();
            context.add(pkg);
            generate_emmylua(ps, value.getTDb().getTTables(), context, 3);
        }

        ps.println();
        ps.println("return %s", pkg);
    }

    private void generate_emmylua(CachedIndentPrinter ps, Collection<TTable> tables, Set<String> context, int lastIndent) {
        String lastUpperPkg = "";
        Queue<TTable> subCfgQueue = new LinkedList<>();
        for (TTable c : tables) {
            String full = Name.fullName(c);
            String[] nameSplits = full.split("\\.");
            int splitLenth = nameSplits.length;
            String lastName = nameSplits[splitLenth - 1];
            if (lastIndent < splitLenth) {
                //如果是更深一层的文件夹，先进队列
                subCfgQueue.add(c);
                continue;
            }
            String curUpperPkg = nameSplits[splitLenth - 2];
            //如果上层的文件夹发生了变化，那么检测队列是否有之前预存的，把这些配置解析了
            if (!subCfgQueue.isEmpty() && !curUpperPkg.equals(lastUpperPkg)) {
                //先把下层在上层的入口给输出了
                String upperPkg2 = "";
                for (TTable tableInQueue : subCfgQueue) {
                    String fullName2 = Name.fullName(tableInQueue);
                    String[] nameSplits2 = fullName2.split("\\.");
                    int splitLenth2 = nameSplits2.length;
                    String curUpperPkg2 = nameSplits2[splitLenth2 - 2];
                    if (!upperPkg2.equals(curUpperPkg2)) {
                        int lastNameLenth = nameSplits2[splitLenth2 - 1].length();
                        ps.println("---@field %s %s", curUpperPkg2, fullName2.substring(0, fullName2.length() - lastNameLenth - 1));
                    }
                    upperPkg2 = curUpperPkg2;
                }

                //然后再递归输出之前缓存的内容
                generate_emmylua(ps, subCfgQueue, context, lastIndent + 1);
                subCfgQueue.clear();
            }
            lastIndent = splitLenth;
            lastUpperPkg = curUpperPkg;

            definePkgEmmyLua(full, ps, context);
            if (!preload) {
                ps.println("---@field %s %s", lastName, full);
            }
            context.add(full);
        }
    }


    private void definePkg(String beanName, CachedIndentPrinter ps, Set<String> context) {
        List<String> seps = Arrays.asList(beanName.split("\\."));
        for (int i = 0; i < seps.size() - 1; i++) {
            String pkg = String.join(".", seps.subList(0, i + 1));
            if (context.add(pkg)) {
                if (useEmmyLua) {
                    ps.println("---@type %s", pkg);
                }
                ps.println(pkg + " = {}");
            }
        }
    }

    private void definePkgEmmyLua(String beanName, CachedIndentPrinter ps, Set<String> context) {
        List<String> seps = Arrays.asList(beanName.split("\\."));
        for (int i = 0; i < seps.size() - 1; i++) {
            String pkg = String.join(".", seps.subList(0, i + 1));
            if (context.add(pkg)) {
                ps.println();
                ps.println("---@class %s", pkg);
            }
        }
    }


    private void generate_loads(CachedIndentPrinter ps) {
        ps.println("local require = require");
        ps.println();
        for (TTable c : value.getTDb().getTTables()) {
            ps.println("require \"%s\"", Name.fullName(c));
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

        if (isLangSwitch) {
            ps.println("local i18n_bean = %s._mk.i18n_bean", pkg);
            ps.println("local i18n_action = %s._mk.i18n_action", pkg);
        }
        ps.println();

        Set<String> context = new HashSet<>();
        context.add("Beans");
        for (TBean tbean : value.getTDb().getTBeans()) {
            String full = Name.fullName(tbean);
            definePkg(full, ps, context);
            context.add(full);

            if (tbean.getBeanDefine().type == Bean.BeanType.BaseDynamicBean) {
                ps.println("%s = {}", full);
                for (TBean actionBean : tbean.getChildDynamicBeans()) {
                    // function mkcfg.action(typeName, refs, ...)
                    String fulln = Name.fullName(actionBean);
                    definePkg(fulln, ps, context);
                    context.add(fulln);
                    String func = "action";
                    String textFieldsStr = "";
                    if (isLangSwitch) {
                        textFieldsStr = TypeStr.getLuaTextFieldsString(actionBean);
                        if (!textFieldsStr.isEmpty()) {
                            func = "i18n_action";
                        }
                    }

                    if (actionBean.getColumns().isEmpty()) {
                        //这里来个优化，加上()直接生成实例，而不是类，注意生成数据时对应不加()
                        ps.println("%s = %s(\"%s\")()", fulln, func, actionBean.name);
                    } else {
                        ps.println("%s = %s(\"%s\", %s, %s%s\n    )", fulln, func, actionBean.name,
                                TypeStr.getLuaRefsString(actionBean),
                                textFieldsStr,
                                TypeStr.getLuaFieldsString(actionBean));
                    }
                }
            } else {
                String func = "bean";
                String textFieldsStr = "";
                if (isLangSwitch) {
                    textFieldsStr = TypeStr.getLuaTextFieldsString(tbean);
                    if (!textFieldsStr.isEmpty()) {
                        func = "i18n_bean";
                    }
                }

                if (tbean.getColumns().isEmpty()) {
                    //这里来个优化，加上()直接生成实例，而不是类，注意生成数据时对应不加()
                    ps.println("%s = %s()()", full, func);
                } else {
                    ps.println("%s = %s(%s, %s%s\n    )", full, func,
                            TypeStr.getLuaRefsString(tbean),
                            textFieldsStr,
                            TypeStr.getLuaFieldsString(tbean));
                }

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

        String fullName = Name.fullName(vtable.getTTable());
        if (useEmmyLua) {
            ps.println("---@class %s", fullName);
            ps.println(TypeStr.getLuaFieldsStringEmmyLua(tbean) + "---@field get function");
            ps.println("---@field all table<any,%s>", fullName);
            ps.println(TypeStr.getLuaRefsStringEmmyLua(tbean));
        }

        ps.println("local this = %s", fullName);
        ps.println();

        String func = "table";
        String textFieldsStr = "";
        if (isLangSwitch) {
            textFieldsStr = TypeStr.getLuaTextFieldsString(tbean);
            if (!textFieldsStr.isEmpty()) {
                func = "i18n_table";
            }
        }

        // function mkcfg.table(self, uniqkeys, enumidx, refs, ...)
        ps.println("local mk = %s._mk.%s(this, %s, %s, %s, %s%s\n    )", pkg, func,
                TypeStr.getLuaUniqKeysString(ttable),
                TypeStr.getLuaEnumIdxString(ttable),
                TypeStr.getLuaRefsString(tbean),
                textFieldsStr,
                TypeStr.getLuaFieldsString(tbean));
        ps.println();

        // 先打印数据到cache，同时收集用到的引用
        ps.enableCache();
        if (isLangSwitch) {
            langSwitch.enterTable(ttable.name);
        }
        for (VBean vBean : vtable.getVBeanList()) {
            lineCache.setLength(0);
            ValueStr.getLuaValueString(lineCache, vBean, "mk", false);
            ps.println(lineCache.toString());
        }
        ps.disableCache();

        // 对收集到的引用local化，lua执行应该会快点
        if (sharedEmptyTable && toBrief.isEmptyTableUsed()) {
            ps.println("local E = %s._mk.E", pkg);
        }

        if (!toBrief.getAll().isEmpty()) {
            for (Map.Entry<String, String> entry : toBrief.getAll().entrySet()) {
                ps.println("local %s = %s", entry.getValue(), entry.getKey());
            }
            ps.println();
        }

        // 再打印cache
        ps.printCache();
        ps.println();
        ps.println("return this");
    }
}
