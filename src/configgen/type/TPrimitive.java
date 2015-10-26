package configgen.type;

import configgen.Node;

public abstract class TPrimitive extends Type {

    public TPrimitive(Node parent, String name, Constraint cons) {
        super(parent, name, cons);
        for (SRef ref : cons.refs) {
            require(null == ref.keyRef, "primitive do not keyRef");
        }
    }

    @Override
    public boolean hasRef() {
        return constraint.refs.size() > 0;
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
