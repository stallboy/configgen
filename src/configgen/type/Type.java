package configgen.type;

import configgen.Node;

public abstract class Type extends Node {
    private Constraint constraint = new Constraint();
    private final int columnIndex; // 从0开始
    private boolean IsPrimitiveAndIsTableKey;

    Type(Node parent, String name, int columnIdx) {
        super(parent, name);
        columnIndex = columnIdx;
    }

    void setPrimitiveAndTableKey() {
        IsPrimitiveAndIsTableKey = true;
    }

    public boolean isPrimitiveAndTableKey() {
        return IsPrimitiveAndIsTableKey;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public String getColumnName() {
        return name;
    }

    void setConstraint(Constraint cons) {
        constraint = cons;
    }


    public abstract boolean hasRef();

    public abstract boolean hasSubBean();

    public abstract boolean hasText();

    public abstract boolean hasBlock();

    public abstract int columnSpan();

    public abstract <T> T accept(TypeVisitorT<T> visitor);


    Type resolveType(TBean parent, String columnName, int columnIdx, String type, boolean packAsOne) {
        switch (type) {
            case "int":
                return new TInt(this, columnName, columnIdx);
            case "long":
                return new TLong(this, columnName, columnIdx);
            case "string":
                return new TString(this, columnName, columnIdx, TString.Subtype.STRING);
            case "bool":
                return new TBool(this, columnName, columnIdx);
            case "float":
                return new TFloat(this, columnName, columnIdx);
            case "text":
                return new TString(this, columnName, columnIdx, TString.Subtype.TEXT);
            default:
                TBean bean = ((AllType) root).resolveBeanRef(parent, type);
                if (bean != null) {
                    return new TBeanRef(this, columnName, columnIdx, bean, packAsOne);
                } else {
                    return null;
                }
        }
    }


}
