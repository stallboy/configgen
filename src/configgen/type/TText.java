package configgen.type;

import configgen.Node;

public class TText extends TPrimitive {

    public TText(Node parent, String link, Constraint cons) {
        super(parent, link, cons);
        Assert(cons.refs.isEmpty(), "text not support ref");
        Assert(cons.nullableRefs.isEmpty(), "text not support nullableRef");
    }

    @Override
    public boolean hasText() {
        return true;
    }

    @Override
    public String toString() {
        return "text";
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