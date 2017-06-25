package configgen.genjava;


import java.util.HashMap;
import java.util.Map;

public class SchemaInterface implements Schema {

    public Map<String, Schema> implementations = new HashMap<>(); //包含SchemaBean和SchemaEnum

    @Override
    public boolean compatible(Schema other) {
        if (other == null || !(other instanceof SchemaInterface)) {
            return false;
        }
        SchemaInterface si = (SchemaInterface) other;
        if (implementations.size() != si.implementations.size()) {
            return false;
        }
        for (Map.Entry<String, Schema> entry : implementations.entrySet()) {
            Schema t1 = entry.getValue();
            Schema t2 = si.implementations.get(entry.getKey());
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
        output.writeInt(INTERFACE);
        output.writeInt(implementations.size());
        for (Map.Entry<String, Schema> entry : implementations.entrySet()) {
            output.writeStr(entry.getKey());
            entry.getValue().write(output);
        }
    }


    public void read(ConfigInput input) {
        implementations = new HashMap<>();
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            String name = input.readStr();
            Schema imp = Schema.create(input);
            implementations.put(name, imp);
        }
    }
}
