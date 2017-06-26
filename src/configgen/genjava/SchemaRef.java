package configgen.genjava;

public class SchemaRef implements Schema {
    public String type;

    @Override
    public boolean compatible(Schema other) {
        return other != null && other instanceof SchemaRef && type.equals(((SchemaRef) other).type);
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
