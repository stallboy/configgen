package configgen.genallref;

import configgen.gen.*;
import configgen.type.SRef;
import configgen.type.TTable;
import configgen.util.CachedFileOutputStream;
import configgen.value.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class GenAllRefValues extends Generator {

    public static void register() {
        Generators.addProvider("allrefvalues", new GeneratorProvider() {
            @Override
            public Generator create(Parameter parameter) {
                return new GenAllRefValues(parameter);
            }

            @Override
            public String usage() {
                return "ref:assets,out:refassets.csv default ignores is empty";
            }
        });
    }

    private final String ref;
    private final Set<String> ignores = new HashSet<>();
    private final String out;

    private GenAllRefValues(Parameter parameter) {
        super(parameter);
        ref = parameter.get("ref", "assets");
        String ignoreStr = parameter.get("ignores", null);
        if (ignoreStr != null) {
            Collections.addAll(ignores, ignoreStr.split(","));
        }
        out = parameter.get("out", "refassets.csv");
        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        Set<String> allrefs = new TreeSet<>();
        VDb value = ctx.makeValue();
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
                value.getValues().forEach((v) -> {
                    v.accept(this);
                });
            }
        };

        for (VTable vTable : value.getVTables()) {
            if (!ignores.contains(vTable.name)) {
                for (VBean vBean : vTable.getVBeanList()) {
                    vBean.accept(vs);
                }
            }
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new CachedFileOutputStream(new File(out)), StandardCharsets.UTF_8)) {
            writer.write(String.join("\r\n", allrefs));
        }
    }
}
