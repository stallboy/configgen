package configgen.type;

import configgen.Node;

public class TList extends Type {
    public final Type value;
    public final int count; // >=0; 0 means list store in one column separated by ;

    public TList(Node parent, String link, Constraint cons, String value, int count) {
        super(parent, link, cons);
        Assert(cons.range == null, "list do not range");
        Assert(cons.nullableRefs.isEmpty(), "list do not nullableRef");
        Assert(cons.keyRefs.isEmpty(), "list do not keyRef");

        this.value = resolve(this, "value", cons, value);
        this.count = count;
    }

    @Override
    public String toString() {
        return "list," + value + (count > 0 ? "," + count : "");
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(TVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean hasText() {
        return value.hasText();
    }

    @Override
    public int columnSpan() {
        return count == 0 ? 1 : (value.columnSpan() * count);
    }


}
