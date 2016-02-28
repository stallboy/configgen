package configgen.type;

import configgen.Node;
import configgen.define.Table;
import configgen.define.UniqueKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TTable extends Node {
    public final Table tableDefine;
    public final TBean tbean;
    public final Map<String, Type> primaryKey = new LinkedHashMap<>();
    public final List<Map<String, Type>> uniqueKeys = new ArrayList<>();

    public TTable(TDb parent, Table cfg) {
        super(parent, cfg.bean.name);
        this.tableDefine = cfg;
        tbean = new TBean(this, cfg.bean);
    }

    public void resolve() {
        tbean.resolve();
        if (!tableDefine.enumStr.isEmpty()) {
            Type type = tbean.columns.get(tableDefine.enumStr);
            require(type != null, "enum not found", tableDefine.enumStr);
            require(type instanceof TString, "enum type not string", type.toString());
        }
        resolveKey(tableDefine.primaryKey, primaryKey);
        for (UniqueKey uk : tableDefine.uniqueKeys.values()) {
            Map<String, Type> res = new LinkedHashMap<>();
            resolveKey(uk.keys, res);
            uniqueKeys.add(res);
        }
    }

    private void resolveKey(String[] keys, Map<String, Type> res) {
        for (String k : keys) {
            Type t = tbean.columns.get(k);
            require(t != null, "primary/unique key not found", k);
            require(null == res.put(k, t), "primary/unique key duplicate", k);
            require(keys.length == 1 || t instanceof TPrimitive, "multi primary/unique key not support bean and container", k);
        }
    }
}
