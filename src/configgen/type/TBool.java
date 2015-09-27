package configgen.type;

import configgen.Node;

public class TBool extends TPrimitive {

    public TBool(Node parent, String link, Constraint cons) {
        super(parent, link, cons);
        Assert(cons.refs.isEmpty(), "bool not support ref");
        Assert(cons.nullableRefs.isEmpty(), "bool not support nullableRef");
        Assert(cons.range == null, "bool not support range");
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public String toString() {
        return "bool";
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(TypeVisitorT<T> visitor) {
        return visitor.visit(this);
    }
}