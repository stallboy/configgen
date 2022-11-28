package configgen.type;

import configgen.Node;

public abstract class TPrimitive extends Type {
    TPrimitive(Node parent, String name, int idx) {
        super(parent, name, idx);
    }

    @Override
    void setConstraint(Constraint cons) {
        super.setConstraint(cons);
        for (SRef sref : cons.references) {
            require(null == sref.mapKeyRefTable, "原始类型不用配置keyRef");
        }
    }

    @Override
    public boolean hasRef() {
        return getConstraint().references.size() > 0;
    }

    @Override
    public boolean hasSubBean() {
        return false;
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public boolean hasBlock() {
        return false;
    }

    @Override
    public int columnSpan() {
        return 1;
    }

    @Override
    public boolean isPrimitiveValueType() {
        return true;
    }

}
