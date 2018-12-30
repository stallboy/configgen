package configgen.type;

import configgen.Node;

public class TLong extends TPrimitive {

    TLong(Node parent, String name, Constraint cons) {
        super(parent, name, cons);
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
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(TypeVisitorT<T> visitor) {
        return visitor.visit(this);
    }

}