package configgen.data;

import configgen.Logger;
import configgen.Node;
import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.AllDefine;
import configgen.define.Table;
import configgen.type.*;

import java.util.*;
import java.util.stream.Collectors;

public class DTable extends Node {
    private final Map<String, DColumn> dcolumns = new LinkedHashMap<>();
    private final List<List<String>> recordList;
    private final List<String> descLine;
    private final List<String> nameLine;

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

    private TTable tableType;

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

    void autoCompleteDefine(Table table) {
        Bean bean = table.bean;
        Map<String, Column> old = new LinkedHashMap<>(bean.columns);
        bean.columns.clear();
        dcolumns.forEach((n, col) -> {
            Column f = old.remove(n);
            if (f == null) {
                f = ((AllDefine) table.parent).newColumn(table, n, col.guessType(), col.desc());
                Logger.verbose("new column " + f.fullName());
            } else {
                bean.columns.put(f.name, f);
                f.desc = col.desc();
            }
        });

        old.forEach((k, f) -> Logger.verbose("delete column " + f.fullName()));

        if (table.primaryKey.length == 0) {
            table.primaryKey = new String[]{table.bean.columns.keySet().iterator().next()};
        }
    }

    void parse(TTable ttable) {
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
