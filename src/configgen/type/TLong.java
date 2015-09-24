package configgen.type;

import configgen.Node;

public class TLong extends TPrimitive {

    public TLong(Node parent, String link, Constraint cons) {
        super(parent, link, cons);
    }

    @Override
    public TLong copy(Node parent) {
        return new TLong(parent, link, constraint);
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public String toString() {
        return "long";
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(TVisitor<T> visitor) {
        return visitor.visit(this);
    }

}