package configgen.genjava.code;

import configgen.gen.LangSwitch;
import configgen.genjava.*;
import configgen.util.CachedIndentPrinter;
import configgen.value.AllValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class GenConfigCodeSchema {
    static void generateAll(GenJavaCode gen, int schemaNumPerFile, AllValue vdb, LangSwitch ls) throws IOException {
        SchemaInterface schemaInterface = SchemaParser.parse(vdb, ls);
        List<Map.Entry<String, Schema>> all = new ArrayList<>(schemaInterface.implementations.entrySet());
        List<Map.Entry<String, Schema>> main;
        List<List<Map.Entry<String, Schema>>> nullableOthers = null;
        if (schemaNumPerFile == -1 || all.size() <= schemaNumPerFile) {
            main = all;
        } else {
            main = all.subList(0, schemaNumPerFile);
            int left = all.size() - schemaNumPerFile;
            int seperateFileNum = (left + schemaNumPerFile - 1) / schemaNumPerFile;

            nullableOthers = new ArrayList<>();
            for (int i = 0; i < seperateFileNum; i++) {
                int start = (i + 1) * schemaNumPerFile;
                int end = start + schemaNumPerFile;
                if (end > all.size()) {
                    end = all.size();
                }
                nullableOthers.add(all.subList(start, end));
            }
        }

        generateFile(gen, 0, main, nullableOthers);
        if (nullableOthers != null) {
            int idx = 0;
            for (List<Map.Entry<String, Schema>> nullableOther : nullableOthers) {
                idx++;
                generateFile(gen, idx, nullableOther, null);
            }
        }
    }

    static String getClassName(int idx) {
        String className = "ConfigCodeSchema";
        if (idx > 0) {
            className = String.format("%s%d", className, idx);
        }

        return className;
    }

    static void generateFile(GenJavaCode gen, int idx,
                             List<Map.Entry<String, Schema>> schemas,
                             List<List<Map.Entry<String, Schema>>> nullableOthers) throws IOException {

        String className = getClassName(idx);
        try (CachedIndentPrinter ps = gen.createCodeFile(className + ".java")) {
            ps.println("package %s;", Name.codeTopPkg);
            ps.println();
            ps.println("import configgen.genjava.*;");
            ps.println();

            ps.println("public class %s {", className);
            ps.println();

            if (idx == 0) {
                printMain(schemas, nullableOthers, ps);
                print(schemas, ps);
            } else {
                print(schemas, ps);
            }
            ps.println("}");
        }
    }

    private static void printMain(List<Map.Entry<String, Schema>> main,
                                  List<List<Map.Entry<String, Schema>>> nullableOthers,
                                  CachedIndentPrinter ip) {

        ip.println1("public static Schema getCodeSchema() {");
        ip.inc();
        ip.inc();

        String name = "schema";
        ip.println("SchemaInterface %s = new SchemaInterface();", name);

        for (Map.Entry<String, Schema> stringSchemaEntry : main) {
            String key = stringSchemaEntry.getKey();
            String func = key.replace('.', '_');
            ip.println("%s.addImp(\"%s\", %s());", name, key, func);
        }

        if (nullableOthers != null) {
            int idx = 0;
            for (List<Map.Entry<String, Schema>> other : nullableOthers) {
                idx++;
                for (Map.Entry<String, Schema> stringSchemaEntry : other) {
                    String key = stringSchemaEntry.getKey();
                    String func = key.replace('.', '_');
                    ip.println("%s.addImp(\"%s\", %s.%s());", name, key, getClassName(idx), func);
                }
            }
        }

        ip.dec();
        ip.dec();
        ip.println2("return %s;", name);
        ip.println1("}");
        ip.println();
    }


    private static void print(List<Map.Entry<String, Schema>> schemas,
                              CachedIndentPrinter ip) {
        PrintSchemaVisitor visitor = new PrintSchemaVisitor(ip);

        for (Map.Entry<String, Schema> stringSchemaEntry : schemas) {
            String key = stringSchemaEntry.getKey();
            String func = key.replace('.', '_');

            ip.println1("static Schema %s() {", func);
            ip.inc();
            ip.inc();

            String name = "s" + ip.indent();
            stringSchemaEntry.getValue().accept(visitor);

            ip.dec();
            ip.dec();
            ip.println2("return %s;", name);
            ip.println1("}");
            ip.println();
        }
    }

    static class PrintSchemaVisitor implements Visitor {

        private final CachedIndentPrinter ip;

        PrintSchemaVisitor(CachedIndentPrinter ip) {
            this.ip = ip;
        }


        @Override
        public void visit(SchemaPrimitive schemaPrimitive) {
            throw new IllegalStateException();
        }

        @Override
        public void visit(SchemaRef schemaRef) {
            throw new IllegalStateException();
        }

        @Override
        public void visit(SchemaList schemaList) {
            throw new IllegalStateException();
        }

        @Override
        public void visit(SchemaMap schemaMap) {
            throw new IllegalStateException();
        }

        @Override
        public void visit(SchemaBean schemaBean) {
            String name = "s" + ip.indent();
            ip.println("SchemaBean %s = new SchemaBean(%s);", name, schemaBean.isTable ? "true" : "false");
            for (SchemaBean.Column column : schemaBean.columns) {
                ip.println("%s.addColumn(\"%s\", %s);", name, column.name, parse(column.schema));
            }
        }

        @Override
        public void visit(SchemaInterface schemaInterface) {
            String name = "s" + ip.indent();
            ip.println("SchemaInterface %s = new SchemaInterface();", name);
            for (Map.Entry<String, Schema> stringSchemaEntry : schemaInterface.implementations.entrySet()) {
                ip.println("{");
                ip.inc();
                String subName = "s" + ip.indent();
                stringSchemaEntry.getValue().accept(this);
                ip.println("%s.addImp(\"%s\", %s);", name, stringSchemaEntry.getKey(), subName);
                ip.dec();
                ip.println("}");
            }
        }

        @Override
        public void visit(SchemaEnum schemaEnum) {
            String name = "s" + ip.indent();
            ip.println("SchemaEnum %s = new SchemaEnum(%s, %s);", name, schemaEnum.isEnumPart ? "true" : "false",
                       schemaEnum.hasIntValue ? "true" : "false");
            for (Map.Entry<String, Integer> entry : schemaEnum.values.entrySet()) {
                if (schemaEnum.hasIntValue) {
                    ip.println("%s.addValue(\"%s\", %d);", name, entry.getKey(), entry.getValue());
                } else {
                    ip.println("%s.addValue(\"%s\");", name, entry.getKey());
                }
            }
        }


        private static String parse(Schema schema) {
            return schema.accept(new VisitorT<String>() {
                @Override
                public String visit(SchemaPrimitive schemaPrimitive) {
                    return "SchemaPrimitive." + schemaPrimitive.name();
                }

                @Override
                public String visit(SchemaRef schemaRef) {
                    return "new SchemaRef(\"" + schemaRef.type + "\")";
                }

                @Override
                public String visit(SchemaList schemaList) {
                    return "new SchemaList(" + parse(schemaList.ele) + ")";
                }

                @Override
                public String visit(SchemaMap schemaMap) {
                    return "new SchemaMap(" + parse(schemaMap.key) + ", " + parse(schemaMap.value) + ")";
                }

                @Override
                public String visit(SchemaBean schemaBean) {
                    throw new IllegalStateException();
                }

                @Override
                public String visit(SchemaInterface schemaInterface) {
                    throw new IllegalStateException();
                }

                @Override
                public String visit(SchemaEnum schemaEnum) {
                    throw new IllegalStateException();
                }
            });
        }
    }


}
