package configgen.type;

import configgen.Node;

public class TFloat extends TPrimitive {

    TFloat(Node parent, String name, int idx) {
        super(parent, name, idx);
    }

    @Override
    void setConstraint(Constraint cons) {
        super.setConstraint(cons);
        require(cons.references.isEmpty(), "浮点数不支持外键");
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public String toString() {
        return "float";
    }

    @Override
    public <T> T accept(TypeVisitorT<T> visitor) {
        return visitor.visit(this);
    }

}