package configgen.genjava.code;

import configgen.define.Bean;
import configgen.define.Table;
import configgen.gen.Context;
import configgen.gen.Generator;
import configgen.gen.Parameter;
import configgen.type.TBean;
import configgen.util.CachedFiles;
import configgen.util.CachedIndentPrinter;
import configgen.value.AllValue;
import configgen.value.VTable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class GenJavaCode extends Generator {
    private File dstDir;
    private final String dir;
    private final String pkg;
    private final String encoding;
    private final int schemaNumPerFile;


    public GenJavaCode(Parameter parameter) {
        super(parameter);
        dir = parameter.get("dir", "config", "目录");
        pkg = parameter.get("pkg", "config", "包名");
        encoding = parameter.get("encoding", "UTF-8", "生成代码文件的编码");
        schemaNumPerFile = Integer.parseInt(
                parameter.get("schemaNumPerFile", "100",
                              "当配表数量过多时生成的ConfigCodeSchema会超过java编译器限制，用此参数来分文件"));

        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        AllValue value = ctx.makeValue(filter);
        dstDir = Paths.get(dir).resolve(pkg.replace('.', '/')).toFile();

        Name.codeTopPkg = pkg;
        GenBeanClassTablePart.mapsInMgr.clear();
        boolean isLangSwitch = ctx.getLangSwitch() != null;
        TypeStr.isLangSwitch = isLangSwitch; //辅助Text的类型声明和创建

        for (TBean tbean : value.getTDb().getTBeans()) {
            try {
                generateBeanClass(tbean);
            } catch (Throwable e) {
                throw new AssertionError(tbean.fullName() + ",这个结构生成java代码出错", e);
            }
            for (TBean childBean : tbean.getChildDynamicBeans()) {
                try {
                    generateBeanClass(childBean);
                } catch (Throwable e) {
                    throw new AssertionError(childBean.fullName() + ",这个子结构生成java代码出错", e);
                }
            }
        }

        for (VTable vtable : value.getVTables()) {
            try {
                generateTableClass(vtable);
            } catch (Throwable e) {
                throw new AssertionError(vtable.getTTable().fullName() + ",这个表生成java代码出错", e);
            }
        }

        if (isLangSwitch) { //生成Text这个Bean
            try (CachedIndentPrinter ps = createCode(new File(dstDir, "Text.java"), encoding)) {
                GenText.generate(ctx.getLangSwitch(), ps);
            }
        }

        try (CachedIndentPrinter ps = createCode(new File(dstDir, "ConfigMgr.java"), encoding)) {
            GenConfigMgr.generate(ps);
        }

        try (CachedIndentPrinter ps = createCode(new File(dstDir, "ConfigLoader.java"), encoding)) {
            GenConfigLoader.generate(ps);
        }

        try (CachedIndentPrinter ps = createCode(new File(dstDir, "ConfigMgrLoader.java"), encoding)) {
            GenConfigMgrLoader.generate(value, ps);
        }

        GenConfigCodeSchema.generateAll(this, schemaNumPerFile, value, ctx.getLangSwitch());

        CachedFiles.deleteOtherFiles(dstDir);
    }

    CachedIndentPrinter createCodeFile(String fileName) {
        return createCode(new File(dstDir, fileName), encoding);
    }


    private void generateBeanClass(TBean tbean) throws IOException {
        BeanName name = new BeanName(tbean);
        try (CachedIndentPrinter ps = createCode(dstDir.toPath().resolve(name.path).toFile(), encoding)) {
            if (tbean.getBeanDefine().type == Bean.BeanType.BaseDynamicBean) {
                GenBaseDynamicBeanInterface.generate(tbean, name, ps);
            } else {
                GenBeanClass.generate(tbean, null, name, ps);
            }
        }
    }

    private void generateTableClass(VTable vtable) throws IOException {
        boolean isNeedReadData = true;
        String dataPostfix = "";
        Table define = vtable.getTTable().getTableDefine();
        if (define.isEnum()) {
            String entryPostfix = "";
            if (define.isEnumFull()) {
                if (define.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                    isNeedReadData = false;
                } else {
                    dataPostfix = "_Detail";
                }
            } else {
                entryPostfix = "_Entry";
            }

            BeanName name = new BeanName(vtable.getTTable().getTBean(), entryPostfix);
            BeanName dataName = new BeanName(vtable.getTTable().getTBean(), dataPostfix);
            File javaFile = dstDir.toPath().resolve(name.path).toFile();
            try (CachedIndentPrinter ps = createCode(javaFile, encoding)) {
                GenEnumClass.generate(vtable, name, ps, define.isEnumFull(), isNeedReadData, dataName);
            }
        }

        if (isNeedReadData) {
            BeanName name = new BeanName(vtable.getTTable().getTBean(), dataPostfix);
            File javaFile = dstDir.toPath().resolve(name.path).toFile();

            try (CachedIndentPrinter ps = createCode(javaFile, encoding)) {
                GenBeanClass.generate(vtable.getTTable().getTBean(), vtable, name, ps);
            }
        }
    }


}
