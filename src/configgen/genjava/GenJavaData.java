package configgen.genjava;

import configgen.Logger;
import configgen.gen.*;
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
        try (ConfigOutput output = new ConfigOutput(new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file))))) {
            Schema schema = GenSchema.parse(value);
            schema.write(output);
            writeValue(value, output);
        }
    }

    private static class SimpleValueVisitor implements ValueVisitor {
        private final ConfigOutput output;

        SimpleValueVisitor(ConfigOutput output) {
            this.output = output;
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
            output.writeStr(value.value);
        }

        @Override
        public void visit(VList value) {
            output.writeInt(value.getList().size());
            value.getList().forEach(v -> v.accept(this));
        }

        @Override
        public void visit(VMap value) {
            output.writeInt(value.map.size());
            value.map.forEach((k, v) -> {
                k.accept(this);
                v.accept(this);
            });
        }

        @Override
        public void visit(VBean value) {
            if (value.childDynamicVBean != null) {
                output.writeStr(value.childDynamicVBean.beanType.name);
                value.childDynamicVBean.getValues().forEach(v -> v.accept(this));
            } else {
                value.getValues().forEach(v -> v.accept(this));
            }
        }
    }
    
    private void writeValue(VDb vDb, ConfigOutput output) throws IOException {
        int cnt = 0;
        for (VTable vTable : vDb.getVTables()) {
            if (vTable.tableType.tableDefine.isEnumFull() && vTable.tableType.tableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                Logger.verbose("ignore write data" + vTable.name);
            } else {
                cnt++;
            }
        }
        output.writeInt(cnt);
        for (VTable vTable : vDb.getVTables()) {
            if (vTable.tableType.tableDefine.isEnumFull() && vTable.tableType.tableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                continue;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (ConfigOutput otherOutput = new ConfigOutput(new DataOutputStream(byteArrayOutputStream))) {
                ValueVisitor visitor = new SimpleValueVisitor(otherOutput);
                otherOutput.writeInt(vTable.getVBeanList().size());
                vTable.getVBeanList().forEach(v -> v.accept(visitor));
                byte[] bytes = byteArrayOutputStream.toByteArray();
                output.writeStr(vTable.name);
                output.writeInt(bytes.length);
                output.write(bytes, 0, bytes.length);
            }
        }

    }

}
