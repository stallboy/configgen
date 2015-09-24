package configgen.type;

import configgen.Node;

public abstract class TPrimitive extends Type{

    public TPrimitive(Node parent, String link, Constraint cons) {
        super(parent, link, cons);
    }

    @Override
    public int columnSpan() {
        return 1;
    }

}
