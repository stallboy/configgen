package configgen.genjava;

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

    private void writeValue(VDb vDb, ConfigOutput output) {
        ValueVisitor vs = new ValueVisitor() {
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

        output.writeInt(vDb.vtables.size());
        for (Map.Entry<String, VTable> entry : vDb.vtables.entrySet()) {
            String name = entry.getKey();
            VTable vTable = entry.getValue();
            output.writeStr(name);
            output.writeInt(vTable.vbeanList.size());
            vTable.vbeanList.forEach(v -> v.accept(vs));
        }

    }

}
