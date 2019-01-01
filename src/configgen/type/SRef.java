package configgen.type;

import configgen.define.ForeignKey;
import configgen.value.Value;

import java.util.Set;

public class SRef {
    public final String name;
    public final TTable refTable;
    public final String[] refCols;
    public final boolean refNullable;

    public final TTable mapKeyRefTable;
    public final String[] mapKeyRefCols;

    public Set<Value> cache; //优化

    public SRef(TForeignKey fk) {
        name = fk.name;
        refTable = fk.refTable;
        refCols = fk.foreignKeyDefine.ref.cols;
        refNullable = fk.foreignKeyDefine.refType == ForeignKey.RefType.NULLABLE;

        mapKeyRefTable = fk.mapKeyRefTable;
        mapKeyRefCols = mapKeyRefTable != null ? fk.foreignKeyDefine.mapKeyRef.cols : null;
    }

    public boolean refToPrimaryKey() {
        return refCols.length == 0;
    }

    public SRef(TTable _refTable, String[] _refCols) {
        refTable = _refTable;
        refCols = _refCols;
        name = "";
        refNullable = false;
        mapKeyRefTable = null;
        mapKeyRefCols = null;
    }

}