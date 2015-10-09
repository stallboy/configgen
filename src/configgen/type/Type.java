package configgen.type;

import configgen.Node;

public abstract class Type extends Node {
    public final Constraint constraint;

    public Type(Node parent, String link, Constraint cons) {
        super(parent, link);
        constraint = cons;
    }

    public boolean hasRef() {
        return constraint.refs.size() > 0;
    }

    public abstract boolean hasText();

    public abstract int columnSpan();

    public abstract void accept(TypeVisitor visitor);

    public abstract <T> T accept(TypeVisitorT<T> visitor);

    protected Type resolveType(String link, Constraint cons, String type, String key, String value, int count) {
        Type t = resolveType(link, cons, type);
        if (t != null)
            return t;

        switch (type) {
            case "list":
                return new TList(this, link, cons, value, count);
            case "map":
                return new TMap(this, link, cons, key, value, count);
        }
        return null;
    }

    protected Type resolveType(String link, Constraint cons, String type) {
        switch (type) {
            case "int":
                return new TInt(this, link, cons);
            case "long":
                return new TLong(this, link, cons);
            case "string":
                return new TString(this, link, cons, TString.Subtype.STRING);
            case "bool":
                return new TBool(this, link, cons);
            case "float":
                return new TFloat(this, link, cons);
            case "text":
                return new TString(this, link, cons, TString.Subtype.TEXT);
        }
        return ((Cfgs) root).tbeans.get(type);
    }
}
