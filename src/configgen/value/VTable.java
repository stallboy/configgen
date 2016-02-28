package configgen.value;

import configgen.Node;
import configgen.data.DColumn;
import configgen.data.DTable;
import configgen.define.UniqueKey;
import configgen.type.TTable;

import java.util.*;
import java.util.stream.Collectors;

public class VTable extends Node {
    public final TTable tableType;
    public final List<VBean> vbeanList = new ArrayList<>();
    public final Set<Value> primaryKeyValueSet = new HashSet<>();
    public final Map<String, Set<Value>> uniqueKeyValueSetMap = new LinkedHashMap<>();

    public final List<Integer> columnIndexes = new ArrayList<>();
    public final boolean isEnum;
    public final boolean isEnumPart;
    public final Set<String> enumNames = new LinkedHashSet<>();
    public final int enumColumnIndex;

    public VTable(VDb parent, TTable ttable, DTable dtable) {
        super(parent, ttable.name);
        tableType = ttable;
        ttable.tbean.columns.forEach((fn, type) -> columnIndexes.addAll(dtable.dcolumns.get(fn).indexes));
        require(columnIndexes.size() > 0);

        dtable.line2data.forEach((row, rowData) -> {
            List<Cell> order = columnIndexes.stream().map(col -> new Cell(row, col, rowData.get(col))).collect(Collectors.toList());
            VBean vbean = new VBean(this, "" + row, ttable.tbean, order);
            vbeanList.add(vbean);
        });

        extractKeyValues(tableType.tableDefine.primaryKey, primaryKeyValueSet);

        for (UniqueKey uk : tableType.tableDefine.uniqueKeys.values()) {
            Set<Value> res = new HashSet<>();
            extractKeyValues(uk.keys, res);
            uniqueKeyValueSetMap.put(uk.toString(), res);
        }

        isEnum = !tableType.tableDefine.enumStr.isEmpty();
        if (isEnum) {
            Set<String> names = new HashSet<>();
            DColumn col = dtable.dcolumns.get(tableType.tableDefine.enumStr);
            enumColumnIndex = col.indexes.get(0);
            boolean part = false;
            for (String e : col.dataList()) {
                e = e.trim();
                if (e.isEmpty()) {
                    part = true;
                } else {
                    require(names.add(e.toUpperCase()), "enum data duplicate", e);
                    enumNames.add(e);
                }
            }
            isEnumPart = part;
        } else {
            isEnumPart = false;
            enumColumnIndex = 0;
        }
    }

    private void extractKeyValues(String[] keys, Set<Value> keyValueSet) {
        for (VBean vbean : vbeanList) {
            List<Value> vs = new ArrayList<>();
            for (String k : keys) {
                Value v = vbean.valueMap.get(k);
                require(v != null);
                vs.add(v);
            }
            Value keyValue;
            if (vs.size() == 1) {
                keyValue = vs.get(0);
            } else {
                keyValue = new VList(this, "key", vs);
            }
            require(keyValueSet.add(keyValue), "primary/unique key value duplicate", keyValue.toString());
        }

    }

    public void verifyConstraint() {

        vbeanList.forEach(VBean::verifyConstraint);
    }
}
