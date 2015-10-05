package configgen.type;

import configgen.Node;

public class TMap extends Type {
    public final Type key;
    public final Type value;
    public final int count; // must > 0

    public TMap(Node parent, String link, Constraint cons, String key, String value, int count) {
        super(parent, link, cons);
        Assert(cons.range == null, "map not support range");

        Constraint kc = new Constraint();
        Constraint vc = new Constraint();
        for (SRef ref : cons.refs) {
            Assert(!ref.nullable, "map not support nullableRef");
            if (null != ref.keyRef)
                kc.refs.add(new SRef("", ref.keyRef, false, null));
            if (null != ref.ref)
                vc.refs.add(new SRef("", ref.ref, false, null));
        }

        this.key = resolveType("key", kc, key);
        this.value = resolveType("value", vc, value);
        this.count = count;
    }

    @Override
    public String toString() {
        return "map," + key + "," + value + "," + count;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(TypeVisitorT<T> visitor) {
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
