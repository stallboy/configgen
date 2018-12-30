package configgen.type;

import configgen.Node;

public class TBool extends TPrimitive {

    TBool(Node parent, String name, Constraint cons) {
        super(parent, name, cons);
        require(cons.references.isEmpty(), "布尔类型不支持外键");
        require(cons.range == null, "布尔类型不支持区间限定");
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