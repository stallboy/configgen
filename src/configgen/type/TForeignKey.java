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

    public Set<Value> cache;  //优化

    TForeignKey(TBean parent, ForeignKey fk) {
        super(parent, fk.name);
        foreignKeyDefine = fk;
    }

    public void resolve() {
        for (String key : foreignKeyDefine.keys) {
            require(null != ((TBean) parent).beanDefine.columns.get(key), "外键列不存在", key);
        }

        refTable = resolveRef(foreignKeyDefine.ref);
        if (foreignKeyDefine.mapKeyRef != null) {
            mapKeyRefTable = resolveRef(foreignKeyDefine.mapKeyRef);
        }
    }

    private TTable resolveRef(Ref ref){
        TTable tt = ((TDb) root).ttables.get(ref.table);
        if (tt != null){
            for (String col : ref.cols) {
                require(null != tt.tbean.beanDefine.columns.get(col), "外键列不存在", col); //must use beanDefine
            }
        }else{
            error("外键表不存在", ref.table);
        }

        return tt;
    }
}
