package configgen.type;

import configgen.Node;

public class TMap extends Type {
    public final Type key;
    public final Type value;
    public final int count; // must > 0

    public TMap(Node parent, String link, Constraint cons, String key, String value, int count) {
        super(parent, link, cons);
        Assert(cons.range == null, "map do not range");
        Assert(cons.nullableRefs.isEmpty(), "map do not nullableRef");

        Constraint kc = new Constraint();
        kc.refs.addAll(cons.keyRefs);
        this.key = resolve(this, "key", kc, key);

        Constraint vc = new Constraint();
        vc.refs.addAll(cons.refs);
        this.value = resolve(this, "value", vc, value);

        this.count = count;
    }

    @Override
    public String toString() {
        return "map," + key + "," + value + "," + count;
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
        return key.hasText() || value.hasText();
    }

    @Override
    public int columnSpan() {
        return (key.columnSpan() + value.columnSpan()) * count;
    }

}
