package configgen.value;

import configgen.Node;
import configgen.data.DDb;
import configgen.type.TDb;

import java.util.LinkedHashMap;
import java.util.Map;

public class VDb extends Node {
    public final TDb dbType;
    public final DDb dbData;
    public final Map<String, VTable> vtables = new LinkedHashMap<>();

    public VDb(TDb tdb, DDb ddb) {
        super(null, "value");
        this.dbType = tdb;
        this.dbData = ddb;
        tdb.ttables.forEach((name, ttable) -> vtables.put(name, new VTable(this, ttable, ddb.dtables.get(name))));
    }

    public void verifyConstraint() {
        vtables.values().forEach(VTable::verifyConstraint);
    }
}
