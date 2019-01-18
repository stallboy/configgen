package configgen.type;

import configgen.Node;

public class TString extends TPrimitive {
    public enum Subtype {
        STRING, TEXT
    }

    public final Subtype subtype;

    TString(Node parent, String name, int idx, Subtype subtype) {
        super(parent, name, idx);
        this.subtype = subtype;
    }

    @Override
    void setConstraint(Constraint cons) {
        super.setConstraint(cons);
        if (subtype == Subtype.TEXT) {
            require(cons.references.isEmpty(), "text类型不支持外键");
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
    public <T> T accept(TypeVisitorT<T> visitor) {
        return visitor.visit(this);
    }
}