package configgen.genjava;

import configgen.Logger;
import configgen.define.Bean;
import configgen.gen.Generator;
import configgen.gen.Parameter;
import configgen.gen.Provider;
import configgen.value.*;

import java.io.*;
import java.util.Map;

public final class GenJavaData extends Generator {

    public static void register() {
        providers.put("javadata", new Provider() {
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
    public void generate(VDb value) throws IOException {
        try (ConfigOutput output = new ConfigOutput(new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file))))) {
            Schema schema = GenSchema.parse(value);
            schema.write(output);
            writeValue(value, output);
        }
    }

    private static class SimpleValueVisitor implements ValueVisitor {
        private final ConfigOutput output;
        SimpleValueVisitor(ConfigOutput output){
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
            output.writeInt(value.list.size());
            value.list.forEach(v -> v.accept(this));
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
            if (value.beanType.beanDefine.type == Bean.BeanType.BaseAction) {
                output.writeStr(value.actionVBean.name);
                value.actionVBean.valueMap.values().forEach(v -> v.accept(this));
            } else {
                value.valueMap.values().forEach(v -> v.accept(this));
            }
        }
    };

    private void writeValue(VDb vDb, ConfigOutput output) throws IOException {
        int cnt = 0;
        for (Map.Entry<String, VTable> entry : vDb.vtables.entrySet()) {
            VTable vTable = entry.getValue();
            if (vTable.tableType.tableDefine.isEnumFull() && vTable.tableType.tableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                Logger.verbose("ignore write data" + vTable.name);
            } else {
                cnt++;
            }
        }
        output.writeInt(cnt);
        for (Map.Entry<String, VTable> entry : vDb.vtables.entrySet()) {
            String name = entry.getKey();
            VTable vTable = entry.getValue();

            if (vTable.tableType.tableDefine.isEnumFull() && vTable.tableType.tableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                continue;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try(ConfigOutput otherOutput = new ConfigOutput(new DataOutputStream(byteArrayOutputStream))) {
                ValueVisitor visitor = new SimpleValueVisitor(otherOutput);
                otherOutput.writeInt(vTable.vbeanList.size());
                vTable.vbeanList.forEach(v -> v.accept(visitor));
                byte[] bytes = byteArrayOutputStream.toByteArray();
                output.writeStr(name);
                output.writeInt(bytes.length);
                output.write(bytes, 0, bytes.length);
            }
        }

    }

}
