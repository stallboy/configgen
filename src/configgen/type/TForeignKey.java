package configgen.type;

import configgen.Node;
import configgen.define.ForeignKey;
import configgen.define.Ref;
import configgen.value.Value;

import java.util.Set;

public class TForeignKey extends Node {
    public final ForeignKey foreignKeyDefine;
    public TTable refTable;
    TTable mapKeyRefTable;

    public Type[] thisTableKeys;

    public Set<Value> cache;  //优化

    TForeignKey(TBean parent, ForeignKey fk) {
        super(parent, fk.name);
        foreignKeyDefine = fk;
    }

    public void resolve() {
        if (foreignKeyDefine.ref != null){
            refTable = resolveRef(foreignKeyDefine.ref);
        }
        if (foreignKeyDefine.mapKeyRef != null) {
            mapKeyRefTable = resolveRef(foreignKeyDefine.mapKeyRef);
        }

        thisTableKeys = new Type[foreignKeyDefine.keys.length];
        int i = 0;
        for (String key : foreignKeyDefine.keys) {
            Type t = ((TBean) parent).getColumnMap().get(key);
            require(null != t, "外键列不存在", key);
            thisTableKeys[i++] = t;
        }
    }

    private TTable resolveRef(Ref ref) {
        TTable tt = ((AllType) root).resolveTableRef((TBean) parent, ref.table);
        require(tt != null, "外键表不存在", ref.table);
        return tt;
    }
}
