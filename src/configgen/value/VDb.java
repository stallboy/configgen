package configgen.value;

import configgen.Node;
import configgen.data.DDb;
import configgen.type.TDb;
import configgen.type.TTable;

import java.util.LinkedHashMap;
import java.util.Map;

public class VDb extends Node {
    public final TDb dbType;
    public final DDb dbData;
    public final I18n i18n;
    public final Map<String, VTable> vtables = new LinkedHashMap<>();

    public VDb(TDb tdb, DDb ddb, I18n i18n) {
        super(null, "value");
        this.dbType = tdb;
        this.dbData = ddb;
        this.i18n = i18n;
        for (TTable tTable : tdb.ttables.values()) {
            try {
                VTable vt = new VTable(this, tTable, ddb.dtables.get(tTable.name));
                vtables.put(tTable.name, vt);
            } catch (Throwable e) {
                throw new AssertionError(tTable.name + ",这个表数据构造出错", e);
            }
        }
    }

    public void verifyConstraint() {
        for (VTable vTable : vtables.values()) {
            try {
                vTable.verifyConstraint();
            }catch (Throwable e){
                throw new AssertionError(vTable.name + ",这个表数据约束检验出错", e);
            }
        }
    }
}
