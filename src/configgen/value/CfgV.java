package configgen.value;

import configgen.Node;
import configgen.data.Column;
import configgen.data.Data;
import configgen.type.Cfg;

import java.util.*;
import java.util.stream.Collectors;

public class CfgV extends Node {
    public final Cfg type;
    public final List<VBean> vbeans = new ArrayList<>();

    public final Set<Value> vkeys = new HashSet<>();

    public final List<Integer> columnIndexes = new ArrayList<>();
    public final boolean isEnum;
    public final boolean isEnumPart;
    public final Set<String> enumNames = new LinkedHashSet<>();
    public final int enumColumnIndex;

    public CfgV(CfgVs parent, String link, Cfg cfg, Data data) {
        super(parent, link);
        type = cfg;
        type.value = this;
        cfg.tbean.fields.forEach((name, type) -> columnIndexes.addAll(data.columns.get(name).indexes));
        Assert(columnIndexes.size() > 0);

        data.line2data.forEach((row, rowData) -> {
            List<Cell> order = columnIndexes.stream().map(col -> new Cell(row, col, rowData.get(col))).collect(Collectors.toList());
            VBean vbean = new VBean(this, ""+row, cfg.tbean, order);
            vbeans.add(vbean);
        });

        for (VBean vbean : vbeans) {
            Value key;
            if (type.define.keys.length == 0) {
                key = vbean.map.values().iterator().next();
            } else {
                List<Value> vs = new ArrayList<>();
                for (String k : type.define.keys) {
                    Value v = vbean.map.get(k);
                    Assert(v != null);
                    vs.add(v);
                }
                key = new VList(this, "keys", vs);
            }
            Assert(vkeys.add(key), "key duplicate", key.toString());
        }

        isEnum = !type.define.enumStr.isEmpty();
        if (isEnum) {
            Set<String> names = new HashSet<>();
            Column col = data.columns.get(type.define.enumStr);
            enumColumnIndex = col.indexes.get(0);
            boolean part = false;
            for (String e : col.dataList()) {
                e = e.trim();
                if (e.isEmpty()) {
                    part = true;
                } else {
                    Assert(names.add(e.toUpperCase()), "enum data duplicate", e);
                    enumNames.add(e);
                }
            }
            isEnumPart = part;
        } else {
            isEnumPart = false;
            enumColumnIndex = 0;
        }
    }

    public void verifyConstraint() {
        vbeans.forEach(VBean::verifyConstraint);
    }
}
