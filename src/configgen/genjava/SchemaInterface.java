package configgen.genjava;

import java.util.LinkedHashMap;
import java.util.Map;

// 这个既是总入口，又是多态bean
public class SchemaInterface implements Schema {

    public final Map<String, Schema> implementations = new LinkedHashMap<>(); //包含SchemaBean和SchemaEnum

    public SchemaInterface(ConfigInput input) {
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            String name = input.readStr();
            Schema imp = Schema.create(input);
            addImp(name, imp);
        }
    }

    public SchemaInterface() {
    }

    public void addImp(String name, Schema schema) {
        Schema old = implementations.put(name, schema);
        if (old != null) {
            throw new IllegalStateException("implementation duplicate " + name);
        }
    }

    @Override
    public boolean compatible(Schema other) {
        if (!(other instanceof SchemaInterface)) {
            return false;
        }
        SchemaInterface si = (SchemaInterface) other;
        if (implementations.size() > si.implementations.size()) {
            throw new SchemaCompatibleException("size not compatible with data err, code=" + implementations.size() + ", data=" + si.implementations.size());
        }
        for (Map.Entry<String, Schema> entry : implementations.entrySet()) {
            Schema t1 = entry.getValue();
            Schema t2 = si.implementations.get(entry.getKey());
            if (!t1.compatible(t2)) {
                throw new SchemaCompatibleException(entry.getKey() + " bean/table not compatible with data err");
            }
        }
        return true;
    }


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(VisitorT<T> visitor) {
        return visitor.visit(this);
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


}
