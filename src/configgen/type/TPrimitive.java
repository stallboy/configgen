package configgen.type;

import configgen.Node;

public abstract class TPrimitive extends Type {

    TPrimitive(Node parent, String name, Constraint cons) {
        super(parent, name, cons);
        for (SRef sref : cons.references) {
            require(null == sref.mapKeyRefTable, "primitive do not keyRef");
        }
    }

    @Override
    public boolean hasRef() {
        return constraint.references.size() > 0;
    }

    @Override
    public boolean hasSubBean() {
        return false;
    }

    @Override
    public int columnSpan() {
        return 1;
    }

}
