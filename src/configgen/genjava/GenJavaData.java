package configgen.genjava;

import configgen.Logger;
import configgen.gen.*;
import configgen.util.CachedFileOutputStream;
import configgen.value.*;

import java.io.*;

public final class GenJavaData extends Generator {

    public static void register() {
        Generators.addProvider("javadata", new GeneratorProvider() {
            @Override
            public Generator create(Parameter parameter) {
                return new GenJavaData(parameter);
            }

            @Override
            public String usage() {
                return "file:config.data";
            }
        });
    }

    private final File file;

    private GenJavaData(Parameter parameter) {
        super(parameter);
        file = new File(parameter.getNotEmpty("file", "config.data"));
        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        VDb value = ctx.makeValue();
        try (ConfigOutput output = new ConfigOutput(new DataOutputStream(new CachedFileOutputStream(file, 2048 * 1024)))) {
            Schema schema = SchemaParser.parse(value, ctx.getLangSwitch());
            schema.write(output);
            writeValue(value, ctx.getLangSwitch(), output);
        }
    }

    private static class SimpleValueVisitor implements ValueVisitor {
        private final ConfigOutput output;
        private final LangSwitch nullableLS;

        SimpleValueVisitor(ConfigOutput output, LangSwitch nullableLS) {
            this.output = output;
            this.nullableLS = nullableLS;
        }

        @Override
        public void visit(VBool value) {
            output.writeBool(value.value);
        }

        @Override
        public void visit(VInt value) {
            output.writeInt(value.value);
        }

        @Override
        public void visit(VLong value) {
            output.writeLong(value.value);
        }

        @Override
        public void visit(VFloat value) {
            output.writeFloat(value.value);
        }

        @Override
        public void visit(VString value) {
            if (value.getType().hasText() && nullableLS != null) { //这里全部写进去，作为一个Text的Bean
                String[] i18nStrings = nullableLS.findAllLangText(value.value);
                for (String i18nStr : i18nStrings) {
                    output.writeStr(i18nStr);
                }
            } else {
                output.writeStr(value.value);
            }
        }

        @Override
        public void visit(VList value) {
            output.writeInt(value.getList().size());
            for (Value v : value.getList()) {
                v.accept(this);
            }
        }

        @Override
        public void visit(VMap value) {
            output.writeInt(value.getMap().size());
            value.getMap().forEach((k, v) -> {
                k.accept(this);
                v.accept(this);
            });
        }

        @Override
        public void visit(VBean value) {
            if (value.getChildDynamicVBean() != null) {
                output.writeStr(value.getChildDynamicVBean().getTBean().name);
                for (Value v : value.getChildDynamicVBean().getValues()) {
                    v.accept(this);
                }
            } else {
                for (Value v : value.getValues()) {
                    v.accept(this);
                }
            }
        }
    }

    private void writeValue(VDb vDb, LangSwitch nullableLS, ConfigOutput output) throws IOException {
        int cnt = 0;
        for (VTable vTable : vDb.getVTables()) {
            if (vTable.getTTable().getTableDefine().isEnumFull() && vTable.getTTable().getTableDefine().isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                Logger.verbose("ignore write data" + vTable.name);
            } else {
                cnt++;
            }
        }
        output.writeInt(cnt);
        for (VTable vTable : vDb.getVTables()) {
            if (vTable.getTTable().getTableDefine().isEnumFull() && vTable.getTTable().getTableDefine().isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                continue;
            }

            if (nullableLS != null) {
                nullableLS.enterTable(vTable.name);
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (ConfigOutput otherOutput = new ConfigOutput(new DataOutputStream(byteArrayOutputStream))) {
                ValueVisitor visitor = new SimpleValueVisitor(otherOutput, nullableLS);
                otherOutput.writeInt(vTable.getVBeanList().size());
                for (VBean v : vTable.getVBeanList()) {
                    v.accept(visitor);
                }
                byte[] bytes = byteArrayOutputStream.toByteArray();
                output.writeStr(vTable.name);
                output.writeInt(bytes.length);
                output.write(bytes, 0, bytes.length);
            }
        }
    }
}
