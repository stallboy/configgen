package configgen.genjava;

import java.util.ArrayList;
import java.util.List;

public class SchemaBean implements Schema {

    public static class Column {
        public String name;
        public Schema schema;

        public boolean compatible(Column other) {
            return name.equals(other.name) && schema.compatible(other.schema);
        }
    }

    public boolean isTable;
    public List<Column> columns = new ArrayList<>();

    @Override
    public boolean compatible(Schema other) {
        if (other == null || !(other instanceof SchemaBean)) {
            return false;
        }
        SchemaBean sb = (SchemaBean) other;
        if (isTable != sb.isTable) {
            return false;
        }
        if (columns.size() != sb.columns.size()) {
            return false;
        }

        for (int i = 0; i < columns.size(); i++) {
            Column t1 = columns.get(i);
            Column t2 = sb.columns.get(i);
            if (!t1.compatible(t2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void write(ConfigOutput output) {
        output.writeInt(BEAN);
        output.writeBool(isTable);
        output.writeInt(columns.size());
        for (Column column : columns) {
            output.writeStr(column.name);
            column.schema.write(output);
        }
    }

    public void read(ConfigInput input) {
        isTable = input.readBool();
        columns = new ArrayList<>();
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            Column c = new Column();
            c.name = input.readStr();
            c.schema = Schema.create(input);
            columns.add(c);
        }
    }


}
