package configgen.value;

import configgen.Node;
import configgen.data.DColumn;
import configgen.data.DTable;
import configgen.define.Table;
import configgen.define.UniqueKey;
import configgen.type.TTable;

import java.util.*;
import java.util.stream.Collectors;

public class VTable extends Node {
    public final TTable tableType;
    public final ArrayList<VBean> vbeanList = new ArrayList<>();
    public final Set<Value> primaryKeyValueSet = new HashSet<>();
    public final Map<String, Set<Value>> uniqueKeyValueSetMap = new LinkedHashMap<>();

    public final Set<String> enumNames = new LinkedHashSet<>();
    public final Map<String, Integer> enumName2IntegerValueMap = new LinkedHashMap<>();


    public configgen.define.Table getTableDefine() {
        return tableType.tableDefine;
    }

    public VTable(VDb parent, TTable ttable, DTable dtable) {
        super(parent, ttable.name);
        tableType = ttable;
        parent.i18n.enter(name);
        List<Integer> columnIndexes = new ArrayList<>();
        ttable.tbean.columns.forEach((fn, type) -> columnIndexes.addAll(dtable.dcolumns.get(fn).indexes));
        require(columnIndexes.size() > 0);

        for (Map.Entry<Integer, List<String>> line : dtable.line2data.entrySet()) {
            int row = line.getKey();
            List<String> rowData = line.getValue();
            List<Cell> order = columnIndexes.stream().map(col -> new Cell(row, col, rowData.get(col))).collect(Collectors.toList());
            VBean vbean = new VBean(this, "" + row, ttable.tbean, order);
            vbeanList.add(vbean);
        }
        vbeanList.trimToSize();



        extractKeyValues(tableType.tableDefine.primaryKey, primaryKeyValueSet);

        for (UniqueKey uk : tableType.tableDefine.uniqueKeys.values()) {
            Set<Value> res = new HashSet<>();
            extractKeyValues(uk.keys, res);
            uniqueKeyValueSetMap.put(uk.toString(), res);
        }

        if (tableType.tableDefine.isEnum()) {

            Set<String> names = new HashSet<>();
            DColumn col = dtable.dcolumns.get(tableType.tableDefine.enumStr);


            for (VBean vbean : vbeanList) {
                Value v = vbean.valueMap.get(tableType.tableDefine.enumStr);
                require(v instanceof VString, "enum value must be TString");
                String e = ((VString) v).value;
                require(!e.contains(" "), "枚举值字符串不应该包含空格");

                if (e.isEmpty()) {
                    require(tableType.tableDefine.enumType == Table.EnumType.EnumPart, "enum && !enumPart value must not empty");
                } else {
                    require(names.add(e.toUpperCase()), "enum data duplicate", e);
                    enumNames.add(e);

                    if (!tableType.tableDefine.isEnumAsPrimaryKey()){
                        Value primaryV = vbean.valueMap.get(tableType.tableDefine.primaryKey[0]);
                        Integer iv = ((VInt)primaryV).value;
                        enumName2IntegerValueMap.put(e, iv);
                    }
                }
            }
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
