package configgen.value;

import configgen.Node;
import configgen.data.AllData;
import configgen.define.AllDefine;
import configgen.gen.Context;
import configgen.type.AllType;
import configgen.type.TTable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AllValue extends Node {
    private static AllValue current; //约定一次处理一个，减少内存占用

    static AllValue getCurrent() {
        return current;
    }

    private final AllType allType;
    private final Context ctx;
    private Map<String, VTable> vTables;

    public AllValue(AllType subType, AllDefine fullDefine, Context ctx) {
        super(null, "value");
        this.allType = subType;
        this.ctx = ctx;
        vTables = new LinkedHashMap<>(subType.getTTables().size());
        current = this;
        for (TTable tTable : subType.getTTables()) {
            try {
                VTable vt = new VTable(this, tTable, fullDefine.getDTable(tTable.name));
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

    public AllType getTDb() {
        return allType;
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

    Context getCtx() {
        return ctx;
    }
}
