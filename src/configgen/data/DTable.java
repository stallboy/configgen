package configgen.data;

import configgen.Logger;
import configgen.Node;
import configgen.define.Column;
import configgen.define.Table;
import configgen.type.TTable;
import configgen.type.Type;

import java.util.*;
import java.util.stream.Collectors;

public class DTable extends Node {
    /**
     * csv前两行的信息，
     * 1. 第一行descLine, 中文，策划写；
     * 2. 第二行nameLine, 英文，程序用。
     * 记录下来用于生成代码
     */
    private final Map<String, DColumn> dcolumns = new LinkedHashMap<>();
    private final List<String> descLine;
    private final List<String> nameLine;

    /**
     * csv2行之后的所有的数据， csv -> list.list.str
     */
    private final List<List<String>> recordList;


    /**
     * 解析好fullType后，附着到Data上，因为之后生成Value时可能只会用 不全的Type
     */
    private TTable tableType;


    private Map<String, Type> defined;
    private State state;
    private int index;

    private String curColumnName;
    private String prevColumnName;
    private int prevColumnSpan;
    private int prevColumnVisited;

    private String A;
    private String B;
    private List<Integer> ABSpan;


    private enum State {
        NORM, MAYBE_LIST_OR_MAP, LIST, MAYBE_MAP, MAYBE_MAP2, MAP
    }


    public TTable getTableType() {
        return tableType;
    }

    void setTableType(TTable tableType) {
        this.tableType = tableType;

        for (DColumn col : dcolumns.values()) {
            col.setColumnType(tableType.getTBean().getColumn(col.name));
        }
    }


    DTable(AllData parent, String name, List<List<String>> raw) {
        super(parent, name);
        if (raw.size() < 2) {
            System.out.println(fullName() + " 数据行数小于2");
            for (List<String> strings : raw) {
                System.out.println(String.join(",", strings));
            }
            throw new AssertionError();
        }

        descLine = raw.get(0);
        nameLine = raw.get(1);
        recordList = raw.subList(2, raw.size());
    }

    // 读取excel数据时使用
    public static List<List<String>> adjustRecords(List<List<String>> raw) {
        List<String> nameLine = raw.get(1);
        // 根据nameLine计算出有效列数量
        int nameColumnsCnt = nameLine.size();
        int uselessColumnsCnt = 0;
        for (int i = nameLine.size() - 1; i >= 0; i--) {
            if (nameLine.get(i).isEmpty()) {
                uselessColumnsCnt++;
            } else {
                break;
            }
        }
        int usefulColumnsCnt = nameColumnsCnt - uselessColumnsCnt;

        // 根据有效列数量，填充未满的记录列，防止后续读取数据时数组越界，excel才会有这种问题
        for (List<String> line : raw.subList(2, raw.size())) {
            if (line.isEmpty()) {
                continue;
            }
            while (line.size() < usefulColumnsCnt) {
                line.add("");
            }
        }

        return raw;
    }


    public List<Integer> getAllColumnIndexes() {
        List<Integer> indexes = new ArrayList<>();
        for (DColumn col : dcolumns.values()) {
            indexes.addAll(col.indexes);
        }
        return indexes;
    }

    public List<List<String>> getRecordList() {
        return recordList;
    }


    //////////////////////////////// auto fix

    void autoFixDefine(Table tableToFix, TTable currentTableType) {
        parse(currentTableType);

        LinkedHashMap<String, Column> columnMapCopy = tableToFix.getColumnMapCopy();

        tableToFix.clearColumns();
        for (DColumn col : dcolumns.values()) {
            Column column = columnMapCopy.remove(col.name);
            if (column != null) {
                String newDesc = col.desc();
                boolean changed = tableToFix.addColumn(column, newDesc);
                if (changed) {
                    Logger.verbose("change column desc " + newDesc);
                }
            } else {
                Column c = tableToFix.addNewColumn(col.name, col.guessType(), col.desc());
                Logger.verbose("new column " + c.fullName());
            }
        }

        for (Column remove : columnMapCopy.values()) {
            Logger.verbose("delete column " + remove.fullName());
        }

        if (tableToFix.primaryKey.length == 0) {
            tableToFix.primaryKey = new String[]{tableToFix.bean.columns.keySet().iterator().next()};
        }
    }

    /**
     * 这里有个状态机，用来在尊重已有 xml配置的基础上，对于没在xml里配置的
     * 1. 来把a1,a2,...自动识别为list
     * 2. 来把a1,b1,a2,b2,...自动识别为map
     * 这个设计，是为了兼容《大主宰》游戏的配置，如果重头设计，我肯定要把这个功能砍掉。
     */
    private void parse(TTable ttable) {
        if (ttable != null) {
            defined = ttable.getTBean().getColumnMap();
        } else {
            defined = Collections.emptyMap();
        }

        state = State.NORM;
        index = -1;
        for (String s : nameLine) {
            index++;
            if (s.isEmpty())
                continue;

            curColumnName = GuessHelper.getColumnName(s);

            switch (state) {
                case LIST:
                    onList();
                    break;
                case MAP:
                    onMap();
                    break;
                case MAYBE_LIST_OR_MAP:
                    onMaybeListOrMap();
                    break;
                case MAYBE_MAP:
                    onMaybeMap();
                    break;
                case MAYBE_MAP2:
                    onMaybeMap2();
                    break;
                case NORM:
                    onNorm();
                    break;
                default:
                    break;
            }
        }

        onEnd();
    }

    private boolean isCurColumnDefined() {
        Type t = defined.get(curColumnName);
        if (t != null) {
            prevColumnName = curColumnName;
            prevColumnSpan = t.columnSpan();
            prevColumnVisited = 1;
            return true;
        }
        return false;
    }

    private boolean isPrevDefinedColumnProcessing() {
        if (prevColumnVisited < prevColumnSpan) {
            prevColumnVisited++;
            return true;
        } else {
            return false;
        }
    }

    private void onNorm() {
        if (isPrevDefinedColumnProcessing()) {
            add(prevColumnName, index);
            return;
        }

        if (isCurColumnDefined()) {
            put(curColumnName, index);
            return;
        }

        if (curColumnName.endsWith("1")) {
            A = curColumnName.substring(0, curColumnName.length() - 1);
            ABSpan = new ArrayList<>();
            ABSpan.add(index);
            state = State.MAYBE_LIST_OR_MAP;
        } else {
            put(curColumnName, index);
        }
    }

    private void onMaybeListOrMap() {
        if (isCurColumnDefined()) {
            put(A + "1", ABSpan.get(0));
            put(curColumnName, index);
            state = State.NORM;
            return;
        }

        if (curColumnName.equals(A + "2")) {
            ABSpan.add(index);
            state = State.LIST;
        } else if (curColumnName.endsWith("1")) {
            B = curColumnName.substring(0, curColumnName.length() - 1);
            ABSpan.add(index);
            state = State.MAYBE_MAP;
        } else {
            put(A + "1", ABSpan.get(0));
            state = State.NORM;
            onNorm();
        }
    }

    private void onList() {
        if (isCurColumnDefined()) {
            put(GuessHelper.makeListName(A), ABSpan);
            put(curColumnName, index);
            state = State.NORM;
            return;
        }

        if (curColumnName.equals(A + (ABSpan.size() + 1))) {
            ABSpan.add(index);
        } else {
            put(GuessHelper.makeListName(A), ABSpan);
            state = State.NORM;
            onNorm();
        }
    }

    private void onMaybeMap() {
        if (isCurColumnDefined()) {
            put(A + "1", ABSpan.get(0));
            put(B + "1", ABSpan.get(1));
            put(curColumnName, index);
            state = State.NORM;
            return;
        }

        if (curColumnName.equals(A + "2")) {
            ABSpan.add(index);
            state = State.MAYBE_MAP2;
        } else if (curColumnName.equals(B + "2")) {
            put(A + "1", ABSpan.remove(0));
            A = B;
            ABSpan.add(index);
            state = State.LIST;
            onNorm();
        } else {
            put(A + "1", ABSpan.get(0));
            put(B + "1", ABSpan.get(1));
            state = State.NORM;
            onNorm();
        }
    }

    private void onMaybeMap2() {
        if (isCurColumnDefined()) {
            put(A + "1", ABSpan.get(0));
            put(B + "1", ABSpan.get(1));
            put(A + "2", ABSpan.get(2));
            put(curColumnName, index);
            state = State.NORM;
            return;
        }

        if (curColumnName.equals(B + "2")) {
            ABSpan.add(index);
            state = State.MAP;
        } else {
            put(A + "1", ABSpan.get(0));
            put(B + "1", ABSpan.get(1));
            put(A + "2", ABSpan.get(2));
            state = State.NORM;
            onNorm();
        }
    }

    private void onMap() {
        if (isCurColumnDefined()) {
            require(ABSpan.size() % 2 == 0);
            put(GuessHelper.makeMapName(A, B), ABSpan);
            put(curColumnName, index);
            state = State.NORM;
            return;
        }

        if (ABSpan.size() % 2 == 0 && curColumnName.equals(A + (ABSpan.size() / 2 + 1))) {
            ABSpan.add(index);
        } else if (ABSpan.size() % 2 == 1 && curColumnName.equals(B + (ABSpan.size() / 2 + 1))) {
            ABSpan.add(index);
        } else {
            require(ABSpan.size() % 2 == 0);
            put(GuessHelper.makeMapName(A, B), ABSpan);
            state = State.NORM;
            onNorm();
        }

    }

    private void onEnd() {
        switch (state) {
            case LIST:
                put(GuessHelper.makeListName(A), ABSpan);
                break;
            case MAP:
                require(ABSpan.size() % 2 == 0);
                put(GuessHelper.makeMapName(A, B), ABSpan);
                break;
            case MAYBE_LIST_OR_MAP:
                put(A + "1", ABSpan.get(0));
                break;
            case MAYBE_MAP:
                put(A + "1", ABSpan.get(0));
                put(B + "1", ABSpan.get(1));
                break;
            case MAYBE_MAP2:
                put(A + "1", ABSpan.get(0));
                put(B + "1", ABSpan.get(1));
                put(A + "2", ABSpan.get(2));
                break;
            case NORM:
                break;
            default:
                break;
        }
    }

    private void put(String s, int i) {
        List<Integer> a = new ArrayList<>(1);
        a.add(i);
        put(s, a);
    }

    private void put(String s, List<Integer> a) {
        DColumn col = new DColumn(this, s);
        col.indexes.addAll(a);
        col.descs.addAll(a.stream().map(descLine::get).collect(Collectors.toList()));
        require(null == dcolumns.put(s, col), "列重复", s);
    }

    private void add(String s, int i) {
        DColumn col = dcolumns.get(s);
        col.indexes.add(i);
        col.descs.add(descLine.get(i));
    }
}
