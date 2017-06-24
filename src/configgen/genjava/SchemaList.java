package configgen.genjava;

public class SchemaList implements Schema {
    public Schema ele;

    @Override
    public boolean compatible(Schema other) {
        if (other == null || !(other instanceof SchemaList)) {
            return false;
        }
        SchemaList ls = (SchemaList) other;
        return ele.compatible(ls.ele);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void write(ConfigOutput output) {
        output.writeInt(7);
        ele.write(output);
    }

    @Override
    public void readExtra(ConfigInput input) {
        ele = Schema.read(input);
    }

}
