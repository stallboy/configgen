package configgen.tool;

import configgen.gen.Context;
import configgen.gen.Generator;
import configgen.gen.Parameter;
import configgen.type.SRef;
import configgen.type.TTable;
import configgen.value.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

public class GenAllRefValues extends Generator {

    private final String ref;
    private final Set<String> ignores = new HashSet<>();
    private final String out;

    private final boolean genSrc;

    private String curTableName;

    private class RefCell {
        String refName;
        HashSet<String> tableNames;

        public RefCell(String _refName) {
            refName = _refName;
            tableNames = new HashSet<>();
        }
    }

    public GenAllRefValues(Parameter parameter) {
        super(parameter);
        ref = parameter.get("ref", "assets", "目标表名");
        String ignoreStr = parameter.get("ignores", null, "忽略这些表");
        if (ignoreStr != null) {
            Collections.addAll(ignores, ignoreStr.split(","));
        }
        out = parameter.get("out", "refassets.csv", "生成文件");
        genSrc = parameter.get("gensrc", "false", "生成引用来源").equals("true");
        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        Map<String, RefCell> allrefs = new HashMap<>();
        AllValue value = ctx.makeValue(filter);
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
                    var refName = value.getRawString();
                    if (value.isCellEmpty())
                        return;
                    allrefs.putIfAbsent(refName, new RefCell(refName));
                    allrefs.get(refName).tableNames.add(curTableName);
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
                if (value.getChildDynamicVBean() != null) {
                    for (Value v : value.getChildDynamicVBean().getValues()) {
                        v.accept(this);
                    }
                } else {
                    for (Value v : value.getValues()) {
                        v.accept(this);
                    }
                }
            }
        };

        for (VTable vTable : value.getVTables()) {
            if (!ignores.contains(vTable.name)) {
                curTableName = vTable.name;
                for (VBean vBean : vTable.getVBeanList()) {
                    vBean.accept(vs);
                }
            }
        }

        try (OutputStreamWriter writer = createUtf8Writer(new File(out))) {
            if (genSrc) {
                StringBuilder content = new StringBuilder();
                content.append("refAsset,table count,tables\r\n");
                var sorted = new ArrayList<RefCell>();
                sorted.addAll(allrefs.values());
                sorted.sort(Comparator.comparing(o -> o.refName));
                sorted.forEach(refCell -> {
                    StringBuilder line = new StringBuilder();
                    line.append(refCell.refName);
                    line.append(',');
                    var cnt = refCell.tableNames.stream().count();
                    line.append(cnt);
                    line.append(',');
                    var idx = 0;
                    for (String tableName : refCell.tableNames) {
                        line.append(tableName);
                        if (++idx == cnt) {
                            line.append("\r\n");
                        } else {
                            line.append(",");
                        }
                    }
                    content.append(line);
                });
                writer.write(content.toString());
            } else
                writer.write(String.join("\r\n", allrefs.keySet()));
        }
    }
}
