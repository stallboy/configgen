package configgen.type;

import configgen.Node;

public class TText extends TPrimitive {

    public TText(Node parent, String link, Constraint cons) {
        super(parent, link, cons);
    }

    @Override
    public TText copy(Node parent) {
        return new TText(parent, link, constraint);
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
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(TVisitor<T> visitor) {
        return visitor.visit(this);
    }

}