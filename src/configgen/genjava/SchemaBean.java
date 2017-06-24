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

    public List<Column> columns = new ArrayList<>();

    @Override
    public boolean compatible(Schema other) {
        if (other == null || !(other instanceof SchemaBean)) {
            return false;
        }
        SchemaBean bs = (SchemaBean) other;
        if (columns.size() != bs.columns.size()) {
            return false;
        }
        for (int i = 0; i < columns.size(); i++) {
            Column t1 = columns.get(i);
            Column t2 = bs.columns.get(i);
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
        output.writeInt(6);
        output.writeInt(columns.size());
        for (Column column : columns) {
            output.writeStr(column.name);
            column.schema.write(output);
        }
    }

    @Override
    public void readExtra(ConfigInput input) {
        columns = new ArrayList<>();
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            Column c = new Column();
            c.name = input.readStr();
            c.schema = Schema.read(input);
            columns.add(c);
        }
    }


}
