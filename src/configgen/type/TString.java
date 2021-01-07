package configgen.type;

import configgen.Node;

public class TString extends TPrimitive {
    public enum Subtype {
        /**
         * 不涉及到国际化
         * 其实感觉大部分string应该都会需要国际化
         * 不需要的情况包括
         * 1. 现有的类型不支持，比如DateTime，需要程序自己从string转下。
         * 2. 用于开发期间的debug
         */
        STRING,
        /**
         * 需要国际化
         */
        TEXT
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