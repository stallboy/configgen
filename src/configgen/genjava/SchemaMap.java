package configgen.genjava;

public class SchemaMap implements Schema {
    public Schema key;
    public Schema value;

    @Override
    public boolean compatible(Schema other) {
        if (other == null || !(other instanceof SchemaMap)) {
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
    public void write(ConfigOutput output) {
        output.writeInt(MAP);
        key.write(output);
        value.write(output);
    }

    public void read(ConfigInput input) {
        key = Schema.create(input);
        value = Schema.create(input);
    }

}
