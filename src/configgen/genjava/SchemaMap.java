package configgen.genjava;

public class SchemaMap implements Schema {
    public Schema key;
    public Schema value;

    @Override
    public boolean compatible(Schema other) {
        if (other == null || !(other instanceof SchemaMap)) {
            return false;
        }
        SchemaMap ms = (SchemaMap) other;
        return key.compatible(ms.key) && value.compatible(ms.value);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void write(ConfigOutput output) {
        output.writeInt(8);
        key.write(output);
        value.write(output);
    }

    @Override
    public void readExtra(ConfigInput input) {
        key = Schema.read(input);
        value = Schema.read(input);
    }

}
