package configgen.genjava;

public class SchemaMap implements Schema {
    public final Schema key;
    public final Schema value;

    public SchemaMap(ConfigInput input) {
        key = Schema.create(input);
        value = Schema.create(input);
    }

    public SchemaMap(Schema key, Schema value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean compatible(Schema other) {
        if (!(other instanceof SchemaMap)) {
            return false;
        }
        SchemaMap sm = (SchemaMap) other;
        return key.compatible(sm.key) && value.compatible(sm.value);
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
        output.writeInt(MAP);
        key.write(output);
        value.write(output);
    }

    @Override
    public String toString() {
        return "Map<" + key + ", " + value + ">";
    }
}
