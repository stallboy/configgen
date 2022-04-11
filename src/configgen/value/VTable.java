package configgen.value;

import configgen.Node;
import configgen.data.DColumn;
import configgen.data.DSheet;
import configgen.data.DTable;
import configgen.define.Table;
import configgen.type.TBean;
import configgen.type.TTable;
import configgen.type.Type;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class VTable extends Node {
    private final TTable tTable;
    /**
     * 对应与一行一行csv的value 列表
     */
    private final List<VBean> vBeanList;
    /**
     * 主键和唯一键的value集合，不能冲突
     */
    final Set<Value> primaryKeyValueSet = new LinkedHashSet<>();
    final Map<String, Set<Value>> uniqueKeyValueSetMap = new LinkedHashMap<>();

    /**
     * 枚举的字符串集合
     */
    private final Set<String> enumNames = new LinkedHashSet<>();
    private final Map<String, Integer> enumNameToIntegerValueMap = new LinkedHashMap<>();


    public VTable(AllValue parent, TTable ttable, DTable tableData) {
        super(parent, ttable.name);
        tTable = ttable;
        parent.getCtx().getI18n().enterTable(name);

        vBeanList = parseVBeanList(tableData);

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
                    require(tTable.getTableDefine().enumType == Table.EnumType.Entry, "全枚举不能有空格");
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

    private List<VBean> parseVBeanList(DTable tableData) {
        boolean hasBlock = tTable.getTBean().hasBlock();
        if (hasBlock) {
            // 用第一列 格子是否为空来判断这行是属于上一个record的block，还是新的一格record
            // 所以要保证新record的第一列必须填。
            // 这里我们约定有block的，primary key所在列必须包含第一列，primary key总是要填的吧。
            boolean isFirstColumnPrimaryKey = false;
            for (String k : tTable.getPrimaryKey().keySet()) {
                DColumn dColumn = tableData.getColumn(k);
                if (dColumn.getIndexes().contains(0)) {
                    isFirstColumnPrimaryKey = true;
                    break;
                }
            }

            require(isFirstColumnPrimaryKey, "block模式下，我们用第一列格子是否为空来判断这行是属于上一个record的block，" +
                    "还是新的一格record。我们约定第一列必须是主键。");
        }

        List<Integer> allColumnIndexes = tableData.getAllColumnIndexes();
        OptionalInt max = allColumnIndexes.stream().mapToInt(Integer::intValue).max();
        int maxColumnIndex = 0;
        if (!max.isPresent()) {
            error("不允许没有列");
        } else {
            maxColumnIndex = max.getAsInt();
        }

        DSheet[] sheets = tableData.getSheets();
        int totalRecords = 0;
        for (DSheet sheet : sheets) {
            totalRecords += sheet.getRecordList().size();
        }

        List<VBean> vBeanList = new ArrayList<>(totalRecords);
        for (DSheet sheet : sheets) {
            List<List<String>> recordList = sheet.getRecordList();
            for (int row = 0; row < recordList.size(); ) {
                List<String> record = recordList.get(row);

                if (record.isEmpty()) {
                    row++;
                    continue;
                }

                if (record.size() <= maxColumnIndex) {
                    error(String.format("当前行的列数和名称行不匹配. 名称行要求至少[%d]列，当前行仅[%d]列,sheet=%s ,row=%d",
                                        maxColumnIndex + 1, record.size(), sheet.name, DSheet.getHeadRow() + row + 1));
                }

                // 转换为AData
                List<Cell> cells = new ArrayList<>(allColumnIndexes.size());
                for (Integer col : allColumnIndexes) {
                    Cell c = new Cell(sheet, row, col, record.get(col));
                    cells.add(c);
                }
                AData<TBean> adata = new AData<>(cells, tableData.getTableType().getTBean(), false);
                VBean vbean = new VBean(tTable.getTBean(), adata);
                vBeanList.add(vbean);
                row++;

                if (hasBlock) {
                    while (row < recordList.size()) {
                        List<String> r = recordList.get(row);
                        // 用第一列 格子是否为空来判断这行是属于上一个record的block，还是新的一格record
                        if (r.get(0).trim().isEmpty()) {
                            row++;  // 具体提取让VList，VMap，通郭parseBlock自己去提取
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        return vBeanList;
    }

    // 要允许block<bean>,bean里仍然有block，如下所示
    // xxxaabbxccc
    //      bb ccc
    //      bb
    //    aabb
    //      bb
    // aabb前面一列要有空格，bb前一列格子也要是空，ccc前一列也是有个空，
    // 用这个空来做为标记，支持block aabb嵌套block bb，来判断此行bb是否属于嵌套的bb还是新起的aabb
    // 这样也强制了2个同级的block不要直接衔接，视觉上不好区分，
    // 策划可以在中间加入一个对程序不可见的列，比如以上的aabb和ccc直接有x来分割
    // 以上规则现在没有做检测，要检测有点复杂，人工保证吧。
    public static List<Cell> parseBlock(List<Cell> cellsInRecordLine) {
        Cell firstCell = cellsInRecordLine.get(0);
        DSheet sheet = firstCell.getSheet();
        int thisRow = firstCell.getRow();
        int firstCol = firstCell.getCol();
        List<List<String>> recordList = sheet.getRecordList();

        List<Cell> res = null;
        for (int row = thisRow + 1; row < recordList.size(); row++) {
            List<String> line = sheet.getRecordList().get(row);

            if (line.get(0).trim().isEmpty()) {
                // 属于上一个record的block
                if (line.get(firstCol - 1).trim().isEmpty() && !line.get(firstCol).trim().isEmpty()) {
                    // 上一格为空，本格不为空 -》 是这个block了

                    if (res == null) {
                        res = new ArrayList<>(cellsInRecordLine);
                    }

                    for (Cell fc : cellsInRecordLine) {
                        Cell c = new Cell(sheet, row, fc.getCol(), line.get(fc.getCol()));
                        res.add(c);
                    }
                }
            } else {
                // 下一个record了
                break;
            }
        }

        return (res != null) ? res : requireNonNull(cellsInRecordLine, "defaultObj");
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
