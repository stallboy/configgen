package configgen.genjava;

public class SchemaRef implements Schema {
    public String type;

    @Override
    public boolean compatible(Schema other) {
        if (other == null || !(other instanceof SchemaRef)) {
            return false;
        }
        SchemaRef sr = (SchemaRef) other;
        return type.equals(sr.type);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void write(ConfigOutput output) {
        output.writeInt(REF);
        output.writeStr(type);
    }

    public void read(ConfigInput input) {
        type = input.readStr();
    }
}
