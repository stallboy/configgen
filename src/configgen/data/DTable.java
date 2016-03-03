package configgen.data;

import configgen.Logger;
import configgen.Node;
import configgen.define.Bean;
import configgen.define.Db;
import configgen.define.Table;
import configgen.define.Column;
import configgen.type.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DTable extends Node {
    public final Map<Integer, List<String>> line2data = new LinkedHashMap<>(); //start from 0
    public final Map<String, DColumn> dcolumns = new LinkedHashMap<>();

    private final List<String> descLine;
    private final List<String> nameLine;

    private static class NameType {
        final String name;
        final Type type;

        NameType(String n, Type t) {
            name = n;
            type = t;
        }
    }

    private final Map<String, NameType> defined = new LinkedHashMap<>();
    private State state;
    private int index;
    private String _name;
    private GuessHelper.Sep nameSep;
    private NameType nameSepColumn;

    private int nameSepVisited;
    private String A;
    private String B;
    private List<Integer> ABSpan;

    private enum State {
        NORM, MAYBE_LIST_OR_MAP, LIST, MAYBE_MAP, MAYBE_MAP2, MAP
    }

    public DTable(DDb parent, String name, List<List<String>> raw) {
        super(parent, name);
        require(raw.size() > 1);
        descLine = raw.get(0);
        nameLine = raw.get(1);
        for (int i = 2; i < raw.size(); i++) {
            List<String> line = raw.get(i);
            if (CSV.isLineHasContent(line)) {
                line2data.put(i, line);
            }
        }
    }

    public void autoCompleteDefine(Table table) {
        Bean bean = table.bean;
        Map<String, Column> old = new LinkedHashMap<>(bean.columns);
        bean.columns.clear();
        dcolumns.forEach((n, col) -> {
            Column f = old.remove(n);
            if (f == null) {
                f = ((Db)table.parent).newColumn(table, n, col.guessType(), col.desc());
                Logger.verbose("new column " + f.fullName());
            }else{
                bean.columns.put(f.name, f);
                f.desc = col.desc();
            }
        });

        old.forEach((k, f) -> Logger.verbose("delete column " + f.fullName()));
    }

    void parse(TTable ttable) {
        if (ttable != null) {
            makeOriginal(ttable.tbean.columns);
        }

        state = State.NORM;
        index = -1;
        for (String s : nameLine) {
            index++;
            if (s.isEmpty())
                continue;

            _name = s;
            nameSep = GuessHelper.trySep(_name);

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

    private void makeOriginal(Map<String, Type> definedColumns) {
        definedColumns.forEach((k, t) -> {
            if (t instanceof TList) {
                TList type = (TList) t;
                if (type.count == 0) {
                    defined.put(k, new NameType(k, t));
                } else {
                    String columnName = (type.value instanceof TBean) ? k : GuessHelper.parseListName(k);
                    require(null == defined.put(columnName, new NameType(k, t)), "list column name duplicate");
                }
            } else if (t instanceof TMap) {
                TMap type = (TMap) t;
                String columnName = (type.key instanceof TBean || type.value instanceof TBean) ? k : GuessHelper.parseMapName(k).key;
                require(null == defined.put(columnName, new NameType(k, t)), "map column name duplicate");
            } else {
                defined.put(k, new NameType(k, t));
            }
        });
    }

    private boolean isDefined() {
        NameType t = defined.get(_name);
        if (t != null) {
            require(t.type.columnSpan() == 1);
            return true;
        }
        return false;
    }

    private boolean isNameSepDefined() {
        if (nameSep.type != GuessHelper.SepType.None) {
            NameType t = defined.get(nameSep.columnName);
            if (t != null) {
                if (nameSep.type == GuessHelper.SepType.IntPostfix)
                    require(nameSep.num == 1);
                nameSepColumn = t;
                nameSepVisited = 1;
                return true;
            }
        }
        return false;
    }

    private boolean isNameSepDefinedProcessing() {
        if (nameSepColumn != null) {
            nameSepVisited++;
            if (nameSepVisited > nameSepColumn.type.columnSpan()) {
                nameSepColumn = null;
                return false;
            }
            return true;
        }
        return false;
    }

    private void onNorm() {
        if (isNameSepDefinedProcessing()) {
            add(nameSepColumn.name, index);
            return;
        }

        if (isDefined()) {
            put(_name, index);
            return;
        }

        if (isNameSepDefined()) {
            put(nameSepColumn.name, index);
            return;
        }

        if (nameSep.type == GuessHelper.SepType.IntPostfix && nameSep.num == 1) {
            A = nameSep.columnName;
            ABSpan = new ArrayList<>();
            ABSpan.add(index);
            state = State.MAYBE_LIST_OR_MAP;
        } else {
            put(_name, index);
        }
    }

    private void onMaybeListOrMap() {
        if (isDefined()) {
            put(A + "1", ABSpan.get(0));
            put(_name, index);
            state = State.NORM;
            return;
        }

        if (isNameSepDefined()) {
            put(A + "1", ABSpan.get(0));
            put(nameSepColumn.name, index);
            state = State.NORM;
            return;
        }

        if (_name.equals(A + "2")) {
            ABSpan.add(index);
            state = State.LIST;
        } else if (nameSep.type == GuessHelper.SepType.IntPostfix && nameSep.num == 1) {
            B = nameSep.columnName;
            ABSpan.add(index);
            state = State.MAYBE_MAP;
        } else {
            put(A + "1", ABSpan.get(0));
            state = State.NORM;
            onNorm();
        }
    }

    private void onList() {
        if (isDefined()) {
            put(GuessHelper.makeListName(A), ABSpan);
            put(_name, index);
            state = State.NORM;
            return;
        }

        if (isNameSepDefined()) {
            put(GuessHelper.makeListName(A), ABSpan);
            put(nameSepColumn.name, index);
            state = State.NORM;
            return;
        }

        if (_name.equals(A + (ABSpan.size() + 1))) {
            ABSpan.add(index);
        } else {
            put(GuessHelper.makeListName(A), ABSpan);
            state = State.NORM;
            onNorm();
        }
    }

    private void onMaybeMap() {
        if (isDefined()) {
            put(A + "1", ABSpan.get(0));
            put(B + "1", ABSpan.get(1));
            put(_name, index);
            state = State.NORM;
            return;
        }

        if (isNameSepDefined()) {
            put(A + "1", ABSpan.get(0));
            put(B + "1", ABSpan.get(1));
            put(nameSepColumn.name, index);
            state = State.NORM;
            return;
        }

        if (_name.equals(A + "2")) {
            ABSpan.add(index);
            state = State.MAYBE_MAP2;
        } else if (_name.equals(B + "2")) {
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
        if (isDefined()) {
            put(A + "1", ABSpan.get(0));
            put(B + "1", ABSpan.get(1));
            put(A + "2", ABSpan.get(2));
            put(_name, index);
            state = State.NORM;
            return;
        }

        if (isNameSepDefined()) {
            put(A + "1", ABSpan.get(0));
            put(B + "1", ABSpan.get(1));
            put(A + "2", ABSpan.get(2));
            put(nameSepColumn.name, index);
            state = State.NORM;
            return;
        }

        if (_name.equals(B + "2")) {
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
        if (isDefined()) {
            require(ABSpan.size() % 2 == 0);
            put(GuessHelper.makeMapName(A, B), ABSpan);
            put(_name, index);
            state = State.NORM;
            return;
        }

        if (isNameSepDefined()) {
            require(ABSpan.size() % 2 == 0);
            put(GuessHelper.makeMapName(A, B), ABSpan);
            put(nameSepColumn.name, index);
            state = State.NORM;
            return;
        }

        if (ABSpan.size() % 2 == 0 && _name.equals(A + (ABSpan.size() / 2 + 1))) {
            ABSpan.add(index);
        } else if (ABSpan.size() % 2 == 1 && _name.equals(B + (ABSpan.size() / 2 + 1))) {
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
        List<Integer> a = new ArrayList<>();
        a.add(i);
        put(s, a);
    }

    private void put(String s, List<Integer> a) {
        DColumn col = new DColumn(this, s);
        col.indexes.addAll(a);
        col.descs.addAll(a.stream().map(descLine::get).collect(Collectors.toList()));
        require(null == dcolumns.put(s, col), "column duplicate " + s);
    }

    private void add(String s, int i) {
        DColumn col = dcolumns.get(s);
        col.indexes.add(i);
        col.descs.add(descLine.get(i));
    }
}
