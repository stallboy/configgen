package configgen.genjava;

public class SchemaList implements Schema {
    public Schema ele;

    @Override
    public boolean compatible(Schema other) {
        return other != null && other instanceof SchemaList && ele.compatible(((SchemaList) other).ele);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void write(ConfigOutput output) {
        output.writeInt(LIST);
        ele.write(output);
    }

    public void read(ConfigInput input) {
        ele = Schema.create(input);
    }

}
