package configgen.type;

import configgen.define.ForeignKey;
import configgen.value.Value;

import java.util.Set;

/**
 * Single Type Ref， 单个类型对应的Reference，来自于单个column
 * 或primitive，或bean，
 * 或map的key，value，或list的element （因为这个来源所以没有用TForeignKey，而是分开）
 */
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
        if (fk.foreignKeyDefine.ref != null) {
            refCols = fk.foreignKeyDefine.ref.cols;
        }else{
            refCols = new String[0];
        }
        refNullable = fk.foreignKeyDefine.refType == ForeignKey.RefType.NULLABLE;

        mapKeyRefTable = fk.mapKeyRefTable;
        mapKeyRefCols = mapKeyRefTable != null ? fk.foreignKeyDefine.mapKeyRef.cols : new String[0];
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