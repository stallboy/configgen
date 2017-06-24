package configgen.genjava;

public enum SchemaPrimitive implements Schema {
    SBool(1),
    SInt(2),
    SLong(3),
    SFloat(4),
    SStr(5);

    private int t;

    SchemaPrimitive(int t) {
        this.t = t;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void write(ConfigOutput output) {
        output.writeInt(t);
    }

    @Override
    public void readExtra(ConfigInput input) {

    }

    @Override
    public boolean compatible(Schema other) {
        return this == other;
    }
}
