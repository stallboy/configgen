package configgen.genallref;

import configgen.gen.*;
import configgen.type.SRef;
import configgen.type.TTable;
import configgen.value.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class GenAllRefValues extends Generator {

    private final String ref;
    private final Set<String> ignores = new HashSet<>();
    private final String out;

    public GenAllRefValues(Parameter parameter) {
        super(parameter);
        ref = parameter.get("ref", "assets", "目标表名");
        String ignoreStr = parameter.get("ignores", null, "忽略这些表");
        if (ignoreStr != null) {
            Collections.addAll(ignores, ignoreStr.split(","));
        }
        out = parameter.get("out", "refassets.csv", "生成文件");
        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        Set<String> allrefs = new TreeSet<>();
        AllValue value = ctx.makeValue();
        TTable refTable = value.getTDb().getTTable(ref);
        if (refTable == null) {
            System.out.println("ref " + ref + " not a table");
            return;
        }

        ValueVisitor vs = new ValueVisitor() {
            private void vp(VPrimitive value) {
                boolean has = false;
                for (SRef sr : value.getType().getConstraint().references) {
                    if (sr.refTable == refTable) {
                        has = true;
                        break;
                    }
                }
                if (has) {
                    allrefs.add(value.getRawString());
                }
            }

            @Override
            public void visit(VBool value) {
                vp(value);
            }

            @Override
            public void visit(VInt value) {
                vp(value);
            }

            @Override
            public void visit(VLong value) {
                vp(value);
            }

            @Override
            public void visit(VFloat value) {
                vp(value);
            }

            @Override
            public void visit(VString value) {
                vp(value);
            }

            @Override
            public void visit(VList value) {
                value.getList().forEach(v -> v.accept(this));
            }

            @Override
            public void visit(VMap value) {
                value.getMap().forEach((k, v) -> {
                    k.accept(this);
                    v.accept(this);
                });
            }

            @Override
            public void visit(VBean value) {
                value.getValues().forEach((v) -> v.accept(this));
            }
        };

        for (VTable vTable : value.getVTables()) {
            if (!ignores.contains(vTable.name)) {
                for (VBean vBean : vTable.getVBeanList()) {
                    vBean.accept(vs);
                }
            }
        }

        try (OutputStreamWriter writer = createUtf8Writer(new File(out))) {
            writer.write(String.join("\r\n", allrefs));
        }
    }
}
