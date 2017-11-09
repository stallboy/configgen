package configgen.genjava;

import java.util.Map;

public final class GenJavaCodeSchema {


    public static void print(SchemaInterface schemaInterface, IndentPrint ip) {
        {
            ip.println1("public static Schema getCodeSchema() {");
            ip.inc();
            ip.inc();

            String name = "schema";
            ip.println("SchemaInterface %s = new SchemaInterface();", name);
            for (Map.Entry<String, Schema> stringSchemaEntry : schemaInterface.implementations.entrySet()) {
                ip.println("{");
                ip.inc();
                String key = stringSchemaEntry.getKey();
                String func = key.replace('.', '_');
                ip.println("%s.addImp(\"%s\", %s());", name, key, func);
                ip.dec();
                ip.println("}");
            }

            ip.dec();
            ip.dec();
            ip.println2("return %s;", name);
            ip.println1("}");
            ip.println();
        }


        Visitor visitor = new Visitor() {
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
                ip.println("SchemaEnum %s = new SchemaEnum(%s, %s);", name, schemaEnum.isEnumPart ? "true" : "false", schemaEnum.hasIntValue ? "true" : "false");
                for (Map.Entry<String, Integer> entry : schemaEnum.values.entrySet()) {
                    if (schemaEnum.hasIntValue) {
                        ip.println("%s.addValue(\"%s\", %d);", name, entry.getKey(), entry.getValue());
                    } else {
                        ip.println("%s.addValue(\"%s\");", name, entry.getKey());
                    }
                }
            }
        };

        for (Map.Entry<String, Schema> stringSchemaEntry : schemaInterface.implementations.entrySet()) {
            String key = stringSchemaEntry.getKey();
            String func = key.replace('.', '_');
            String name = "s" + ip.indent();
            ip.println1("private static Schema %s() {", func);
            ip.inc();
            ip.inc();

            stringSchemaEntry.getValue().accept(visitor);

            ip.dec();
            ip.dec();
            ip.println2("return %s;", name);
            ip.println1("}");
            ip.println();
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
