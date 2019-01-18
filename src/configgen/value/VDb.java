package configgen.value;

import configgen.Node;
import configgen.data.DDb;
import configgen.type.TDb;
import configgen.type.TTable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class VDb extends Node {
    private static VDb current; //约定一次处理一个，减少内存占用

    static VDb getCurrent() {
        return current;
    }

    private final TDb tDb;
    private final I18n i18n;
    private Map<String, VTable> vTables;

    public VDb(TDb tdb, DDb ddb, I18n i18n) {
        super(null, "value");
        this.tDb = tdb;
        this.i18n = i18n;
        vTables = new LinkedHashMap<>(tdb.getTTables().size());
        current = this;
        for (TTable tTable : tdb.getTTables()) {
            try {
                VTable vt = new VTable(this, tTable, ddb.getDTable(tTable.name));
                vTables.put(tTable.name, vt);
            } catch (Throwable e) {
                throw new AssertionError(tTable.name + ",这个表数据构造出错", e);
            }
        }
    }

    public void verifyConstraint() {
        for (VTable vTable : vTables.values()) {
            try {
                vTable.verifyConstraint();
            } catch (Throwable e) {
                throw new AssertionError(vTable.name + ",这个表数据约束检验出错", e);
            }
        }
    }

    public TDb getTDb() {
        return tDb;
    }

    public Collection<VTable> getVTables() {
        return vTables.values();
    }

    public Set<String> getTableNames() {
        return vTables.keySet();
    }

    public VTable getVTable(String tableName) {
        return vTables.get(tableName);
    }

    I18n getI18n() {
        return i18n;
    }
}
