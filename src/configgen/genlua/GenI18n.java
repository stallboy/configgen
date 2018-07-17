package configgen.genlua;

import configgen.define.Bean;
import configgen.gen.Context;
import configgen.gen.Generator;
import configgen.gen.Parameter;
import configgen.gen.Provider;
import configgen.value.*;

import java.io.File;
import java.util.*;

public final class GenI18n extends Generator {

    public static void register() {
        providers.put("i18n", new Provider() {
            @Override
            public Generator create(Parameter parameter) {
                return new GenI18n(parameter);
            }

            @Override
            public String usage() {
                return "file:../i18n/i18n-config.csv,encoding:GBK";
            }
        });
    }

    private File file;
    private String encoding;

    private GenI18n(Parameter parameter) {
        super(parameter);
        file = new File(parameter.get("file", "../i18n/i18n-config.csv"));
        encoding = parameter.get("encoding", "GBK");
        parameter.end();
    }

    @Override
    public void generate(Context ctx) {
        VDb value = ctx.makeValue();

        Set<String> textSet = new HashSet<>();
        ValueVisitor visitor = new TextValueVisitor(textSet);

        for (VTable vTable : value.vtables.values()) {
            if (vTable.tableType.tbean.hasText()) {
                vTable.vbeanList.forEach(v -> v.accept(visitor));
            }
        }


        List<List<String>> rows = new ArrayList<>(textSet.size());
        for (String s : new TreeSet<>(textSet)) {
            List<String> r = new ArrayList<>(2);
            r.add(s);
            r.add("");
            rows.add(r);
        }

        CSVWriter.writeToFile(file, encoding, rows);
    }

    private static class TextValueVisitor implements ValueVisitor {
        Set<String> textSet;

        TextValueVisitor(Set<String> set) {
            textSet = set;
        }

        @Override
        public void visit(VBool value) {
        }

        @Override
        public void visit(VInt value) {
        }

        @Override
        public void visit(VLong value) {
        }

        @Override
        public void visit(VFloat value) {
        }

        @Override
        public void visit(VString value) {
            if (!value.type.hasText()) {
                return;
            }

            if (!value.value.isEmpty()) {
                textSet.add(value.value);
            }
        }

        @Override
        public void visit(VList value) {
            for (Value v : value.list) {
                v.accept(this);
            }
        }

        @Override
        public void visit(VMap value) {
            value.map.forEach((k, v) -> {
                k.accept(this);
                v.accept(this);
            });
        }

        @Override
        public void visit(VBean value) {
            if (!value.type.hasText()) {
                return;
            }
            if (value.beanType.beanDefine.type == Bean.BeanType.BaseAction) {
                for (Value v : value.actionVBean.valueMap.values()) {
                    v.accept(this);
                }
            } else {
                for (Value v : value.valueMap.values()) {
                    v.accept(this);
                }
            }
        }
    }

    ;

}
