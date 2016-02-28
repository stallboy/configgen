package configgen.type;

import configgen.Node;

public class TBool extends TPrimitive {

    public TBool(Node parent, String name, Constraint cons) {
        super(parent, name, cons);
        require(cons.references.isEmpty(), "bool not support ref");
        require(cons.range == null, "bool not support range");
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