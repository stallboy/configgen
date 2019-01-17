package configgen.type;

import configgen.Node;
import configgen.define.Column;

public abstract class Type extends Node {
    public final Constraint constraint;
    private final int columnIndex;

    int getColumnIndex() {
        return columnIndex;
    }


    Type(Node parent, String name, int columnIdx, Constraint cons) {
        super(parent, name);
        columnIndex = columnIdx;
        constraint = cons;
    }

    public abstract boolean hasRef();

    public abstract boolean hasSubBean();

    public abstract boolean hasText();

    public abstract int columnSpan();

    public abstract void accept(TypeVisitor visitor);

    public abstract <T> T accept(TypeVisitorT<T> visitor);

    Type resolveType(String columnName, int columnIdx, Constraint cons,
                     String type, String key, String value, int count,
                     Column.CompressType compressType, char compressSeparator) {
        switch (type) {
            case "list":
                return new TList(this, columnName, columnIdx, cons, value, count, compressType, compressSeparator);
            case "map":
                return new TMap(this, columnName, columnIdx, cons, key, value, count);
        }

        return resolveType(columnName, columnIdx, cons, type, compressType == Column.CompressType.AsOne);
    }

    Type resolveType(String columnName, int columnIdx, Constraint cons, String type, boolean compressAsOne) {
        switch (type) {
            case "int":
                return new TInt(this, columnName, columnIdx, cons);
            case "long":
                return new TLong(this, columnName, columnIdx, cons);
            case "string":
                return new TString(this, columnName, columnIdx, cons, TString.Subtype.STRING);
            case "bool":
                return new TBool(this, columnName, columnIdx, cons);
            case "float":
                return new TFloat(this, columnName, columnIdx, cons);
            case "text":
                return new TString(this, columnName, columnIdx, cons, TString.Subtype.TEXT);
            default:
                TBean bean = ((TDb) root).getTBean(type);
                if (bean != null) {
                    return new TBeanRef(this, columnName, columnIdx, cons, bean, compressAsOne);
                } else {
                    return null;
                }
        }
    }
}
