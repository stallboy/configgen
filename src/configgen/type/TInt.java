package configgen.type;

import configgen.Node;

public class TInt extends TPrimitive {

    public TInt(Node parent, String link, Constraint cons) {
        super(parent, link, cons);
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public String toString() {
        return "int";
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