package configgen.gen;

import configgen.type.SRef;
import configgen.type.TTable;
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

    static void register() {
        providers.put("allrefvalues", new Provider() {
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

    public GenAllRefValues(Parameter parameter) {
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
    public void generate(VDb value) throws IOException {
        Set<String> allrefs = new TreeSet<>();
        TTable refTable = value.dbType.ttables.get(ref);
        if (refTable == null) {
            System.out.println("ref " + ref + " not a table");
            return;
        }

        ValueVisitor vs = new ValueVisitor() {
            private void vp(VPrimitive value) {
                boolean has = false;
                for (SRef sr : value.type.constraint.references) {
                    if (sr.refTable == refTable) {
                        has = true;
                        break;
                    }
                }
                if (has) {
                    allrefs.add(value.raw.data);
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
                value.list.forEach(v -> v.accept(this));
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
                value.valueMap.forEach((name, v) -> v.accept(this));
            }
        };

        value.vtables.forEach((name, vtable) -> {
            if (!ignores.contains(name)) {
                vtable.vbeanList.forEach(vbean -> vbean.accept(vs));
            }
        });

        try (OutputStreamWriter writer = new OutputStreamWriter(new CachedFileOutputStream(new File(out)), StandardCharsets.UTF_8)) {
            writer.write(String.join("\r\n", allrefs));
        }
    }
}
