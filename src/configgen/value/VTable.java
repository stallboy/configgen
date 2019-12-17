package configgen.value;

import configgen.Node;
import configgen.data.DTable;
import configgen.define.Table;
import configgen.type.TBean;
import configgen.type.TTable;
import configgen.type.Type;
import configgen.util.CSVParser;

import java.util.*;

public class VTable extends Node {
    private final TTable tTable;
    private List<VBean> vBeanList;
    final Set<Value> primaryKeyValueSet = new LinkedHashSet<>();
    final Map<String, Set<Value>> uniqueKeyValueSetMap = new LinkedHashMap<>();

    private final Set<String> enumNames = new LinkedHashSet<>();
    private final Map<String, Integer> enumNameToIntegerValueMap = new LinkedHashMap<>();


    public VTable(AllValue parent, TTable ttable, DTable tableData) {
        super(parent, ttable.name);
        tTable = ttable;
        parent.getCtx().getI18n().enterTable(name);
        List<Integer> allColumnIndexes = tableData.getAllColumnIndexes();
        require(allColumnIndexes.size() > 0);

        vBeanList = new ArrayList<>(tableData.getRecordList().size());
        int row = 1;
        for (List<String> rowData : tableData.getRecordList()) {
            row++; // 从2开始
            if (CSVParser.isEmptyRecord(rowData)) {
                continue;
            }

            // 转换为AData
            List<Cell> cells = new ArrayList<>(allColumnIndexes.size());
            for (Integer columnIndex : allColumnIndexes) {
                Cell c = new Cell(row, columnIndex, rowData.get(columnIndex));
                cells.add(c);
            }
            AData<TBean> adata = new AData<>(cells, tableData.getTableType().getTBean(), false);
            VBean vbean = new VBean(ttable.getTBean(), adata);
            vBeanList.add(vbean);
        }

        // 收集主键和唯一键
        extractKeyValues(tTable.getPrimaryKey().values(), primaryKeyValueSet);
        for (Map<String, Type> uniqueKey : tTable.getUniqueKeys()) {
            Set<Value> res = new HashSet<>();
            extractKeyValues(uniqueKey.values(), res);
            uniqueKeyValueSetMap.put(String.join(",", uniqueKey.keySet()), res);
        }

        // 收集枚举
        if (tTable.getTableDefine().isEnum()) {
            Set<String> names = new HashSet<>();
            for (VBean vbean : vBeanList) {
                Value v = vbean.getColumnValue(tTable.getEnumColumnType());
                VString vStr = (VString) v;
                if (vStr == null) {
                    error("枚举必须是字符串");
                    break;
                }
                String e = vStr.value;
                require(!e.contains(" "), "枚举值字符串不应该包含空格");

                if (e.isEmpty()) {
                    require(tTable.getTableDefine().enumType == Table.EnumType.EnumPart, "全枚举不能有空格");
                } else {
                    require(names.add(e.toUpperCase()), "枚举数据重复", e);
                    enumNames.add(e);

                    if (!tTable.getTableDefine().isEnumAsPrimaryKey()) { //必须是int，这里是java生成需要
                        Type primaryKeyCol = tTable.getPrimaryKey().values().iterator().next();
                        Value primaryV = vbean.getColumnValue(primaryKeyCol);
                        Integer iv = ((VInt) primaryV).value;
                        enumNameToIntegerValueMap.put(e, iv);
                    }
                }
            }
        }
    }

    public TTable getTTable() {
        return tTable;
    }

    public List<VBean> getVBeanList() {
        return vBeanList;
    }

    public Set<String> getEnumNames() {
        return enumNames;
    }

    public Map<String, Integer> getEnumNameToIntegerValueMap() {
        return enumNameToIntegerValueMap;
    }

    private void extractKeyValues(Collection<Type> keys, Set<Value> keyValueSet) {
        for (VBean vbean : vBeanList) {
            ArrayList<Value> vs = new ArrayList<>(keys.size());
            for (Type k : keys) {
                Value v = vbean.getColumnValue(k);
                require(v != null);
                vs.add(v);
            }
            Value keyValue;
            if (vs.size() == 1) {
                keyValue = vs.get(0);
            } else {
                keyValue = new VList(vs);
            }
            require(keyValueSet.add(keyValue), "主键或唯一键重复", keyValue);
        }

    }

    void verifyConstraint() {
        for (VBean vBean : vBeanList) {
            vBean.verifyConstraint();
        }
        if (tTable.getTableDefine().isPrimaryKeySeq) {
            int seq = 1;
            for (Value value : primaryKeyValueSet) {
                VInt v = (VInt) value;
                if (v == null) {
                    error("设置了isPrimaryKeySeq 则主键必须是int");
                } else {
                    require(v.value == seq, "设置了isPrimaryKeySeq 则主键必须是1,2,3...");
                }
                seq++;
            }
        }
    }


}
