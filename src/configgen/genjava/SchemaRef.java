package configgen.genjava;

public class SchemaRef implements Schema {
    public String type;

    public SchemaRef(ConfigInput input) {
        type = input.readStr();
    }

    public SchemaRef(String type) {
        this.type = type;
    }

    @Override
    public boolean compatible(Schema other) {
        return other != null && other instanceof SchemaRef && type.equals(((SchemaRef) other).type);
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
        output.writeInt(REF);
        output.writeStr(type);
    }

}
