package configgen.type;

import configgen.Node;
import configgen.define.Db;

import java.util.LinkedHashMap;
import java.util.Map;

public class TDb extends Node {
    public final Db dbDefine;
    public final Map<String, TBean> tbeans = new LinkedHashMap<>();
    public final Map<String, TTable> ttables = new LinkedHashMap<>();

    public TDb(Db def) {
        super(null, "tdb");
        dbDefine = def;
        dbDefine.beans.forEach((k, v) -> tbeans.put(k, new TBean(this, v)));
        dbDefine.tables.forEach((k, v) -> ttables.put(k, new TTable(this, v)));
    }

    public void resolve() {
        tbeans.values().forEach(TBean::resolve);
        ttables.values().forEach(TTable::resolve);
    }
}
