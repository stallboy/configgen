package configgen.type;

import configgen.Node;

public abstract class Type extends Node {
    public final Constraint constraint;

    public Type(Node parent, String link, Constraint cons) {
        super(parent, link);
        constraint = cons;
    }

    public boolean hasRef() {
        return constraint.refs.size() > 0 ||  constraint.nullableRefs.size() > 0 || constraint.keyRefs.size() > 0;
    }

    public abstract Type copy(Node parent);

    public abstract boolean hasText();

    public abstract int columnSpan();

    public abstract void accept(Visitor visitor);

    public abstract <T> T accept(TVisitor<T> visitor);


    static Type resolve(Node parent, String link, Constraint cons, String type, String key, String value, int count) {
        Type t = resolve(parent, link, cons, type);
        if (t!= null)
            return t;

        switch (type) {
            case "list":
                return new TList(parent, link, cons, value, count);
            case "map":
                return new TMap(parent, link, cons, key, value, count);
        }
        return null;
    }

    static Type resolve(Node parent, String link, Constraint cons, String type) {
        switch (type){
            case "int":
                return new TInt(parent, link, cons);
            case "long":
                return new TLong(parent, link, cons);
            case "string":
                return new TString(parent, link, cons);
            case "bool":
                return new TBool(parent, link, cons);
            case "float":
                return new TFloat(parent, link, cons);
            case "text":
                return new TText(parent, link, cons);
        }
        return ((Cfgs)parent.root).tbeans.get(type);
        //return new TBean(parent, link, cons, ((Cfgs)parent.root).tbeans.get(type));
    }

}
