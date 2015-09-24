package configgen.type;

import configgen.Node;

public class TFloat extends TPrimitive {

    public TFloat(Node parent, String link, Constraint cons) {
        super(parent, link, cons);
    }

    @Override
    public TFloat copy(Node parent) {
        return new TFloat(parent, link, constraint);
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
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(TVisitor<T> visitor) {
        return visitor.visit(this);
    }

}