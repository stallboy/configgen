package configgen.type;

import configgen.Node;

public class TString extends TPrimitive {
    public enum Subtype {
        STRING, TEXT
    }

    public final Subtype subtype;

    public TString(Node parent, String name, Constraint cons, Subtype subtype) {
        super(parent, name, cons);
        this.subtype = subtype;
        switch (subtype) {
            case STRING:
                break;
            case TEXT:
                require(cons.refs.isEmpty(), "text not support ref");
                break;
        }
    }

    @Override
    public boolean hasText() {
        return subtype == Subtype.TEXT;
    }

    @Override
    public String toString() {
        return subtype.toString().toLowerCase();
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