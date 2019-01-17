package configgen.type;

import configgen.Node;

public abstract class TPrimitive extends Type {

    TPrimitive(Node parent, String name, int idx, Constraint cons) {
        super(parent, name, idx, cons);
        for (SRef sref : cons.references) {
            require(null == sref.mapKeyRefTable, "原始类型不用配置keyRef");
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
