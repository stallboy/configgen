package configgen.type;

import configgen.Node;

public class TLong extends TPrimitive {

    TLong(Node parent, String name, int idx) {
        super(parent, name, idx);
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
    public <T> T accept(TypeVisitorT<T> visitor) {
        return visitor.visit(this);
    }

}