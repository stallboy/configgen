package configgen.type;

import configgen.Node;
import configgen.define.ForeignKey;
import configgen.define.Ref;
import configgen.value.Value;

import java.util.Set;

public class TForeignKey extends Node {
    public final ForeignKey foreignKeyDefine;
    public Type[] thisTableKeys;
    public TTable refTable;
    TTable mapKeyRefTable;

    public Set<Value> cache;  //优化

    TForeignKey(TBean parent, ForeignKey fk) {
        super(parent, fk.name);
        foreignKeyDefine = fk;
    }

    public void resolve(TBean thisBean) {
        thisTableKeys = resolveRefKeys(thisBean, foreignKeyDefine.keys);

        if (foreignKeyDefine.ref != null) {
            refTable = resolveRefTable(foreignKeyDefine.ref);

        }
        if (foreignKeyDefine.mapKeyRef != null) {
            mapKeyRefTable = resolveRefTable(foreignKeyDefine.mapKeyRef);
        }
    }

    public Type[] getRefTypeKeys() {
        if (foreignKeyDefine.ref != null) {
            return resolveRefKeys(refTable.getTBean(), foreignKeyDefine.ref.cols);
        } else {
            return null;
        }
    }

    private TTable resolveRefTable(Ref ref) {
        TTable tt = ((AllType) root).resolveTableRef((TBean) parent, ref.table);
        require(tt != null, "外键表不存在", ref.table);
        return tt;
    }

    private Type[] resolveRefKeys(TBean tBean, String[] cols) {
        Type[] res = new Type[cols.length];
        int i = 0;
        for (String col : cols) {
            Type t = tBean.getColumnMap().get(col);
            require(null != t, "外键列不存在", tBean.fullName(), col);
            res[i++] = t;
        }
        return res;
    }
}
