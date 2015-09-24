package configgen.type;

import configgen.Node;

public class TBool extends TPrimitive {

    public TBool(Node parent, String link, Constraint cons) {
        super(parent, link, cons);
    }

    @Override
    public TBool copy(Node parent) {
        return new TBool(parent, link, constraint);
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
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(TVisitor<T> visitor) {
        return visitor.visit(this);
    }
}