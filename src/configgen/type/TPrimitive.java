package configgen.type;

import configgen.Node;

public abstract class TPrimitive extends Type {

    public TPrimitive(Node parent, String link, Constraint cons) {
        super(parent, link, cons);
        for (SRef ref : cons.refs) {
            Assert(null == ref.keyRef, "primitive do not keyRef");
        }
    }

    @Override
    public boolean hasRef() {
        return constraint.refs.size() > 0;
    }

    @Override
    public int columnSpan() {
        return 1;
    }

}
