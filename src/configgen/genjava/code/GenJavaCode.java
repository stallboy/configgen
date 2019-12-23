package configgen.genjava.code;

import configgen.define.Bean;
import configgen.define.Table;
import configgen.gen.*;
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

    public GenJavaCode(Parameter parameter) {
        super(parameter);
        dir = parameter.get("dir", "config", "目录");
        pkg = parameter.get("pkg", "config", "包名");
        encoding = parameter.get("encoding", "UTF-8", "生成代码文件的编码");

        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        AllValue value = ctx.makeValue();
        dstDir = Paths.get(dir).resolve(pkg.replace('.', '/')).toFile();

        Name.codeTopPkg = pkg;
        GenBeanClassTablePart.mapsInMgr.clear();
        boolean isLangSwitch = ctx.getLangSwitch() != null;
        TypeStr.isLangSwitch = isLangSwitch; //辅助Text的类型声明和创建

        for (TBean tbean : value.getTDb().getTBeans()) {
            generateBeanClass(tbean);
            for (TBean childBean : tbean.getChildDynamicBeans()) {
                generateBeanClass(childBean);
            }
        }

        for (VTable vtable : value.getVTables()) {
            generateTableClass(vtable);
        }

        if (isLangSwitch) { //生成Text这个Bean
            try (CachedIndentPrinter ps = createCode(new File(dstDir, "Text.java"), encoding)) {
                GenText.generate(ctx.getLangSwitch(), ps);
            }
        }

        try (CachedIndentPrinter ps = createCode(new File(dstDir, "ConfigMgr.java"), encoding)) {
            GenConfigMgr.generate(ps);
        }

        try (CachedIndentPrinter ps = createCode(new File(dstDir, "ConfigMgrLoader.java"), encoding)) {
            GenConfigMgrLoader.generate(value, ps);
        }

        try (CachedIndentPrinter ps = createCode(new File(dstDir, "ConfigCodeSchema.java"), encoding)) {
            GenConfigCodeSchema.generate(value, ctx.getLangSwitch(), ps); //Text作为一个SchemaBean
        }

        CachedFiles.deleteOtherFiles(dstDir);
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
