package configgen.genlua;

import configgen.define.Bean;
import configgen.gen.Context;
import configgen.gen.Generator;
import configgen.gen.LangSwitch;
import configgen.gen.Parameter;
import configgen.type.TBean;
import configgen.type.TTable;
import configgen.type.Type;
import configgen.util.CachedFiles;
import configgen.util.CachedIndentPrinter;
import configgen.value.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GenLua extends Generator {

    private final String dir;
    private final String pkg;
    private final String encoding;
    private final boolean useEmmyLua;
    private final boolean preload;
    private final boolean useShared;
    private final boolean useSharedEmptyTable;
    private final boolean packBool;
    private final boolean tryColumnMode;
    private AllValue value;

    private File dstDir;
    private boolean isLangSwitch;
    private LangSwitch langSwitch;
    private final boolean noStr;

    public GenLua(Parameter parameter) {
        super(parameter);
        dir = parameter.get("dir", ".", "生成代码所在目录");
        pkg = parameter.get("pkg", "cfg", "模块名称");
        encoding = parameter.get("encoding", "UTF-8", "编码");

        useEmmyLua = parameter.has("emmylua", "是否生成EmmyLua相关的注解");
        preload = parameter.has("preload", "是否一开始就全部加载配置，默认用到的时候再加载");
        useSharedEmptyTable = parameter.has("sharedEmptyTable", "是否提取空table {}");
        useShared = parameter.has("shared", "是否提取非空的公共table");

        packBool = parameter.has("packbool", "是否要把同一个结构里的多个bool压缩成一个int");
        tryColumnMode = parameter.has("col", "是否尝试列模式,如果开启将压缩同一列的bool和不超过26bit的整数\n" +
                "            默认-Dgenlua.column_min_row=100,-Dgenlua.column_min_save=100");
        noStr = parameter.has("nostr", "!!!只用来测试字符串占用内存大小");
        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        AContext.getInstance().init(pkg, ctx.getLangSwitch(), useSharedEmptyTable, useShared, tryColumnMode, packBool, noStr);

        langSwitch = ctx.getLangSwitch();
        isLangSwitch = langSwitch != null;

        Path dstDirPath = Paths.get(dir).resolve(pkg.replace('.', '/'));
        dstDir = dstDirPath.toFile();
        value = ctx.makeValue(filter);

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
            try (CachedIndentPrinter ps = createCode(new File(dstDir, Name.tablePath(v.name)), encoding, fileDst, cache, tmp)) {
                generate_table(v, ps, lineCache);
            }
        }

        AContext.getInstance().getStatistics().print();

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
            ValueStringify.getLuaString(lineCache, str);
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

            if (useEmmyLua) {
                ps.println("---@type %s", full);
            }
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
                if (useEmmyLua) {
                    ps.println("---@class %s", full);
                    ps.println();
                    ps.println("---@type %s", full);
                }
                ps.println("%s = {}", full);
                ps.println();

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

                    if (useEmmyLua) {
                        ps.println("---@class %s : %s", fulln, full);
                        ps.printlnIf(TypeStr.getLuaFieldsStringEmmyLua(actionBean));
                        ps.printlnIf(TypeStr.getLuaRefsStringEmmyLua(actionBean));
                        ps.println();
                        ps.println("---@type %s", fulln);
                    }

                    if (actionBean.getColumns().isEmpty()) {
                        //这里来个优化，加上()直接生成实例，而不是类，注意生成数据时对应不加()
                        ps.println("%s = %s(\"%s\")()", fulln, func, actionBean.name);
                    } else {
                        ps.println("%s = %s(\"%s\", %s, %s%s\n    )", fulln, func, actionBean.name,
                                   TypeStr.getLuaRefsString(actionBean, false),
                                   textFieldsStr,
                                   TypeStr.getLuaFieldsString(actionBean, null));
                    }
                    ps.println();
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

                if (useEmmyLua) {
                    ps.println("---@class %s", full);
                    ps.printlnIf(TypeStr.getLuaFieldsStringEmmyLua(tbean));
                    ps.printlnIf(TypeStr.getLuaRefsStringEmmyLua(tbean));
                    ps.println();
                    ps.println("---@type %s", full);
                }

                if (tbean.getColumns().isEmpty()) {
                    //这里来个优化，加上()直接生成实例，而不是类，注意生成数据时对应不加()
                    ps.println("%s = %s()()", full, func);
                } else {
                    ps.println("%s = %s(%s, %s%s\n    )", full, func,
                               TypeStr.getLuaRefsString(tbean, false),
                               textFieldsStr,
                               TypeStr.getLuaFieldsString(tbean, null));
                }
                ps.println();

            }
        }
        ps.println();
        ps.println("return Beans");
    }

    private void generate_table(VTable vtable, CachedIndentPrinter ps, StringBuilder lineCache) throws IOException {
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
            ps.println(TypeStr.getLuaFieldsStringEmmyLua(tbean));
            ps.printlnIf(TypeStr.getLuaUniqKeysStringEmmyLua(ttable));
            ps.printlnIf(TypeStr.getLuaEnumStringEmmyLua(vtable));

            ps.println("---@field %s table<any,%s>", Name.primaryKeyMapName, fullName);
            ps.printlnIf(TypeStr.getLuaRefsStringEmmyLua(tbean));
            ps.println();
        }

        ps.println("local this = %s", fullName);
        ps.println();

        int extraSplit = ttable.getTableDefine().getExtraSplit();
        boolean tryUseShared = useShared && extraSplit == 0;
        boolean tryColumnStore = tryColumnMode && extraSplit == 0;

        Ctx ctx = new Ctx(vtable);
        if (tryUseShared) {
            ctx.parseShared();
        }

        if (tryColumnStore) {
            ctx.parseColumnStore();
        }


        String func = "table";
        String textFieldsStr = "";
        if (isLangSwitch) {
            textFieldsStr = TypeStr.getLuaTextFieldsString(tbean);
            if (!textFieldsStr.isEmpty()) {
                func = "i18n_table";
            }
        }
        if (ctx.getCtxColumnStore().isUseColumnStore()) {
            func = func + "c";
        }

        // function mkcfg.table(self, uniqkeys, enumidx, refs, ...)
        ps.println("local mk = %s._mk.%s(this, %s, %s, %s, %s%s\n    )", pkg, func,
                   TypeStr.getLuaUniqKeysString(ctx),
                   TypeStr.getLuaEnumString(ctx),
                   TypeStr.getLuaRefsString(tbean, ctx.getCtxColumnStore().isUseColumnStore()),
                   textFieldsStr,
                   TypeStr.getLuaFieldsString(tbean, ctx));
        ps.println();

        if (isLangSwitch) {
            langSwitch.enterTable(ttable.name);
        }


        int extraFileCnt = 0;
        if (ctx.getCtxColumnStore().isUseColumnStore()) {
            ///////////////////////////////////// 使用列模式

            // 先打印数据到cache，同时收集用到的引用
            ps.enableCache(0, 0);

            int columnSize = ttable.getTBean().getColumns().size();
            int dataSize = vtable.getVBeanList().size();

            ps.println("mk(%d, {", dataSize);
            int ci = 0;
            for (Type column : ttable.getTBean().getColumns()) { //　一列一列来

                lineCache.setLength(0);
                lineCache.append("{");

                PackInfo packInfo = ctx.getCtxColumnStore().getPackInfo(column.getColumnIndex());
                if (packInfo != null) { //这些字段压缩
                    for (VBean vBean : vtable.getVBeanList()) {
                        Value val = vBean.getValues().get(ci);
                        if (val instanceof VBool) {
                            boolean v = ((VBool) val).value;
                            packInfo.addBool(v);
                        } else {
                            int v = ((VInt) val).value;
                            packInfo.addInt(v);
                        }
                    }
                    packInfo.packTo(lineCache);

                } else { //这些正常
                    ValueStringify stringify = new ValueStringify(lineCache, ctx, null);
                    int di = 0;
                    for (VBean vBean : vtable.getVBeanList()) {
                        Value val = vBean.getValues().get(ci);
                        val.accept(stringify);
                        di++;
                        if (di < dataSize) {
                            lineCache.append(", ");
                        }
                    }
                }


                lineCache.append("}");
                ci++;
                if (ci < columnSize) {
                    lineCache.append(",");
                }

                ps.println(lineCache.toString());

            }
            ps.println("})");


        } else {
            ///////////////////////////////////// 正常模式
            ValueStringify stringify = new ValueStringify(lineCache, ctx, "mk");
            extraFileCnt = ps.enableCache(extraSplit, vtable.getVBeanList().size());

            for (VBean vBean : vtable.getVBeanList()) {
                lineCache.setLength(0);
                vBean.accept(stringify);
                ps.println(lineCache.toString());
            }
        }

        ps.disableCache();


        if (!ctx.getCtxName().getLocalNameMap().isEmpty()) { // 对收集到的引用local化，lua执行会快点
            for (Map.Entry<String, String> entry : ctx.getCtxName().getLocalNameMap().entrySet()) {
                ps.println("local %s = %s", entry.getValue(), entry.getKey());
            }
            ps.println();
        }

        if (useSharedEmptyTable && ctx.getCtxShared().getEmptyTableUseCount() > 0) { // 共享空表
            ps.println("local E = %s._mk.E", pkg);
            ps.println();
        }

        if (tryUseShared && ctx.getCtxShared().getSharedList().size() > 0) { // 共享相同的表
            ps.println("local R = %s._mk.R", pkg); // 给lua个机会设置__newindex，做运行时检测
            ps.println("local A = {}");
            for (CtxShared.VCompositeStr vstr : ctx.getCtxShared().getSharedList()) {
                ps.println("%s = R(%s)", vstr.getName(), vstr.getValueStr());
            }
            ps.println();
        }

        // 再打印cache
        ps.printCache();

        if (extraFileCnt > 0) {
            ps.println();
        }
        for (int extraIdx = 0; extraIdx < extraFileCnt; extraIdx++) {
            ps.println("require \"%s_%d\"(mk)", fullName, extraIdx + 1);

            try (CachedIndentPrinter extraPs = createCode(new File(dstDir, Name.tableExtraPath(vtable.name, extraIdx + 1)), encoding)) {

                extraPs.println("local %s = require \"%s._cfgs\"", pkg, pkg);
                if (ttable.getTBean().hasSubBean()) {
                    extraPs.println("local Beans = %s._beans", pkg);
                }
                extraPs.println();

                if (!ctx.getCtxName().getLocalNameMap().isEmpty()) { // 对收集到的引用local化，lua执行会快点
                    for (Map.Entry<String, String> entry : ctx.getCtxName().getLocalNameMap().entrySet()) {
                        extraPs.println("local %s = %s", entry.getValue(), entry.getKey());
                    }
                    extraPs.println();
                }

                if (useSharedEmptyTable && ctx.getCtxShared().getEmptyTableUseCount() > 0) { // 共享空表
                    extraPs.println("local E = %s._mk.E", pkg);
                    extraPs.println();
                }

                extraPs.println("return function(mk)");
                ps.printExtraCacheTo(extraPs, extraIdx);
                extraPs.println("end");
            }
        }

        ps.println();
        ps.println("return this");
    }
}
