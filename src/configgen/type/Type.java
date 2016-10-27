package configgen.type;

import configgen.Node;

public abstract class Type extends Node {
    public final Constraint constraint;

    Type(Node parent, String name, Constraint cons) {
        super(parent, name);
        constraint = cons;
    }

    public abstract boolean hasRef();

    public abstract boolean hasSubBean();

    public abstract boolean hasText();

    public abstract int columnSpan();

    public abstract void accept(TypeVisitor visitor);

    public abstract <T> T accept(TypeVisitorT<T> visitor);

    Type resolveType(String _name, Constraint cons, String type, String key, String value, int count, char compressSeparator) {
        Type t = resolveType(_name, cons, type);
        if (t != null)
            return t;

        switch (type) {
            case "list":
                return new TList(this, _name, cons, value, count, compressSeparator);
            case "map":
                return new TMap(this, _name, cons, key, value, count);
        }
        return null;
    }

    Type resolveType(String _name, Constraint cons, String type) {
        switch (type) {
            case "int":
                return new TInt(this, _name, cons);
            case "long":
                return new TLong(this, _name, cons);
            case "string":
                return new TString(this, _name, cons, TString.Subtype.STRING);
            case "bool":
                return new TBool(this, _name, cons);
            case "float":
                return new TFloat(this, _name, cons);
            case "text":
                return new TString(this, _name, cons, TString.Subtype.TEXT);
        }
        return ((TDb) root).tbeans.get(type);
    }
}
