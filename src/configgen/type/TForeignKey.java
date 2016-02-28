package configgen.type;

import configgen.Node;
import configgen.define.ForeignKey;
import configgen.define.Ref;

public class TForeignKey extends Node {
    public final ForeignKey foreignKeyDefine;
    public TTable refTable;
    public TTable mapKeyRefTable;

    public TForeignKey(TBean parent, ForeignKey fk) {
        super(parent, fk.name);
        foreignKeyDefine = fk;
    }

    public void resolve() {
        for (String key : foreignKeyDefine.keys) {
            require(null != ((TBean) parent).beanDefine.columns.get(key), "key not exist", key);
        }

        refTable = resolveRef(foreignKeyDefine.ref);
        if (foreignKeyDefine.mapKeyRef != null) {
            mapKeyRefTable = resolveRef(foreignKeyDefine.mapKeyRef);
        }
    }

    private TTable resolveRef(Ref ref){
        TTable tt = ((TDb) root).ttables.get(ref.table);
        require(tt != null, "ref table not found", ref.table);
        for (String col : ref.cols) {
            require(null != tt.tbean.beanDefine.columns.get(col), "ref column not exist", col); //must use beanDefine
        }
        return tt;
    }
}
