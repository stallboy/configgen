package configgen.genjava;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class BinaryToText {
    private static SchemaInterface rootSchema;

    public static void parse(String javaDataFile, String match) throws IOException {

        try (ConfigInput input = new ConfigInput(new DataInputStream(new BufferedInputStream(new FileInputStream(javaDataFile))))) {
            rootSchema = (SchemaInterface) Schema.create(input);

            int tableCount = input.readInt();
            for (int i = 0; i < tableCount; i++) {
                String tableName = input.readStr();
                int tableSize = input.readInt();
                if (tableName.startsWith(match)) {
                    printTableInfo(tableName, tableSize, input);
                    println("");
                } else {
                    input.skipBytes(tableSize);
                }
            }
        }
    }

    private static void printTableInfo(String tableName, int tableSize, ConfigInput input) {
        Schema schema = rootSchema.implementations.get(tableName);

        schema.accept(new Visitor() {
            @Override
            public void visit(SchemaPrimitive schemaPrimitive) {
            }

            @Override
            public void visit(SchemaRef schemaRef) {
            }

            @Override
            public void visit(SchemaList schemaList) {
            }

            @Override
            public void visit(SchemaMap schemaMap) {
            }

            @Override
            public void visit(SchemaBean schemaBean) {
                initDepSchemas();
                printSchemaBean(tableName, schemaBean);
                printDepSchemas();

                println("%s data(size=%d):", tableName, tableSize);
                printTableData(input, schemaBean);
            }

            @Override
            public void visit(SchemaInterface schemaInterface) {
            }

            @Override
            public void visit(SchemaEnum schemaEnum) {
                initDepSchemas();
                printSchemaEnum(tableName, schemaEnum);
                printDepSchemas();

                String schemaName = tableName + "_Detail";
                Schema realSchema = rootSchema.implementations.get(schemaName);
                if (realSchema instanceof SchemaBean) {
                    println("%s data(size=%d):", tableName, tableSize);
                    printTableData(input, (SchemaBean) realSchema);
                }
            }
        });
    }


    private static int indent = 0;
    private static StringBuilder tmp = new StringBuilder();

    private static void println(String fmt, Object... args) {
        tmp.setLength(0);
        if (args.length > 0) {
            prefix(tmp, fmt);
            System.out.printf(tmp.toString(), args);
        } else {
            prefix(tmp, fmt);
            System.out.print(tmp);
        }
    }

    private static void prefix(StringBuilder sb, String fmt) {
        for (int i = 0; i < indent; i++) {
            sb.append("    ");
        }
        sb.append(fmt);
        sb.append('\n');
    }


    private static Set<Schema> printedSchemas = new HashSet<>();
    private static HashMap<String, Schema> needSchemas = new LinkedHashMap<>();

    private static void initDepSchemas() {
        needSchemas.clear();
    }

    private static void printDepSchemas() {
        for (Map.Entry<String, Schema> entry : needSchemas.entrySet()) {
            visitSchemaToPrintSchema(entry.getKey(), entry.getValue());
        }
    }

    private static void printSchemaBean(String name, SchemaBean schemaBean) {
        println(name + " {");
        indent++;

        if (printedSchemas.contains(schemaBean)) {
            return;
        }

        printedSchemas.add(schemaBean);

        for (SchemaBean.Column column : schemaBean.columns) {
            println("%s: %s", column.name, column.schema);
            if (column.schema instanceof SchemaRef) {
                SchemaRef ref = (SchemaRef) column.schema;
                Schema rs = rootSchema.implementations.get(ref.type);
                if (!printedSchemas.contains(rs)) {
                    needSchemas.put(ref.type, rs);
                }
            }
        }

        indent--;
        println("}");
    }

    private static void printSchemaInterface(String name, SchemaInterface schemaInterface) {
        boolean isNotRoot = name != null;
        if (isNotRoot) {
            if (printedSchemas.contains(schemaInterface)) {
                return;
            }

            printedSchemas.add(schemaInterface);
            println(name + " {");

        }

        for (Map.Entry<String, Schema> stringSchemaEntry : schemaInterface.implementations.entrySet()) {
            if (isNotRoot) {
                indent++;
            }

            visitSchemaToPrintSchema(stringSchemaEntry.getKey(), stringSchemaEntry.getValue());

            if (isNotRoot) {
                indent--;
            }
        }

        if (isNotRoot) {
            println("}");
        }
    }


    private static void printSchemaEnum(String name, SchemaEnum schemaEnum) {
        println("%s(isEnumPart=%s, hasIntValue=%s) {", name, schemaEnum.isEnumPart, schemaEnum.hasIntValue);
        indent++;


        for (Map.Entry<String, Integer> stringIntegerEntry : schemaEnum.values.entrySet()) {
            if (schemaEnum.hasIntValue) {
                println("%s: %d", stringIntegerEntry.getKey(), stringIntegerEntry.getValue());
            } else {
                println("%s", stringIntegerEntry.getKey());
            }
        }

        indent--;
        println("}");
    }


    private static void visitSchemaToPrintSchema(String name, Schema schema) {
        schema.accept(new Visitor() {
            @Override
            public void visit(SchemaPrimitive schemaPrimitive) {

            }

            @Override
            public void visit(SchemaRef schemaRef) {

            }

            @Override
            public void visit(SchemaList schemaList) {

            }

            @Override
            public void visit(SchemaMap schemaMap) {

            }

            @Override
            public void visit(SchemaBean schemaBean) {
                printSchemaBean(name, schemaBean);
            }

            @Override
            public void visit(SchemaInterface schemaInterface) {
                printSchemaInterface(name, schemaInterface);
            }

            @Override
            public void visit(SchemaEnum schemaEnum) {
                printSchemaEnum(name, schemaEnum);
            }

        });
    }


    private static void visitSchemaToReadData(Schema sc, ConfigInput input, StringBuilder sb) {
        sc.accept(new Visitor() {
            @Override
            public void visit(SchemaPrimitive schemaPrimitive) {
                switch (schemaPrimitive) {
                    case SBool:
                        sb.append(input.readBool());
                        break;
                    case SInt:
                        sb.append(input.readInt());
                        break;
                    case SLong:
                        sb.append(input.readLong());
                        break;
                    case SFloat:
                        sb.append(input.readFloat());
                        break;
                    case SStr:
                        sb.append(input.readStr());
                        break;
                }
            }

            @Override
            public void visit(SchemaRef schemaRef) {
                Schema deRef = rootSchema.implementations.get(schemaRef.type);
                visitSchemaToReadData(deRef, input, sb);
            }

            @Override
            public void visit(SchemaList schemaList) {
                sb.append("(");
                int cnt = input.readInt();
                for (int i = 0; i < cnt; i++) {
                    visitSchemaToReadData(schemaList.ele, input, sb);
                    if (i < cnt - 1) {
                        sb.append(",");
                    }
                }
                sb.append(")");
            }

            @Override
            public void visit(SchemaMap schemaMap) {
                sb.append("(");
                int cnt = input.readInt();
                for (int i = 0; i < cnt; i++) {
                    visitSchemaToReadData(schemaMap.key, input, sb);
                    sb.append("=");
                    visitSchemaToReadData(schemaMap.value, input, sb);
                    if (i < cnt - 1) {
                        sb.append(",");
                    }
                }
                sb.append(")");
            }

            @Override
            public void visit(SchemaBean schemaBean) {
                sb.append("(");
                int cnt = schemaBean.columns.size();
                int idx = 0;
                for (SchemaBean.Column column : schemaBean.columns) {
                    idx++;
                    visitSchemaToReadData(column.schema, input, sb);
                    if (idx < cnt) {
                        sb.append(",");
                    }
                }
                sb.append(")");
            }

            @Override
            public void visit(SchemaInterface schemaInterface) {
                String type = input.readStr();
                sb.append(type);
                Schema implSchema = schemaInterface.implementations.get(type);
                visitSchemaToReadData(implSchema, input, sb);
            }

            @Override
            public void visit(SchemaEnum schemaEnum) {

            }
        });
    }


    private static void printTableData(ConfigInput input, SchemaBean tableSchema) {
        for (int c = input.readInt(); c > 0; c--) {
            StringBuilder sb = new StringBuilder();
            visitSchemaToReadData(tableSchema, input, sb);
            println(sb.toString());
        }

    }


}
