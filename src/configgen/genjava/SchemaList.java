package configgen.genjava;

public class SchemaList implements Schema {
    public final Schema ele;

    public SchemaList(ConfigInput input) {
        ele = Schema.create(input);
    }

    public SchemaList(Schema ele) {
        this.ele = ele;
    }

    @Override
    public boolean compatible(Schema other) {
        return other instanceof SchemaList && ele.compatible(((SchemaList) other).ele);
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
        output.writeInt(LIST);
        ele.write(output);
    }

    @Override
    public String toString() {
        return "List<" + ele + ">";
    }

}
