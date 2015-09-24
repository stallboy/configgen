package configgen.data;

import configgen.CSV;
import configgen.Node;
import configgen.define.Config;
import configgen.define.Field;
import configgen.type.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Data extends Node {
    public final Map<Integer, List<String>> line2data = new LinkedHashMap<>(); //start from 0
    public final Map<String, Column> columns = new LinkedHashMap<>();

    private final List<String> descLine;
    private final List<String> nameLine;

    private static class NameType {
        String name;
        Type type;

        NameType(String n, Type t) {
            name = n;
            type = t;
        }
    }

    private Map<String, NameType> defined = new LinkedHashMap<>();
    private State state;
    private int index;
    private String name;
    private Rules.Sep nameSep;
    private NameType nameSepField;

    private int nameSepVisited;
    private String A;
    private String B;
    private List<Integer> ABSpan;

    private enum State {
        NORM, MAYBELISTORMAP, LIST, MAYBEMAP, MAYBEMAP2, MAP
    }

    public Data(Datas parent, String link, List<List<String>> raw) {
        super(parent, link);
        descLine = raw.get(0);
        nameLine = raw.get(1);
        for (int i = 2; i < raw.size(); i++) {
            List<String> line = raw.get(i);
            if (!CSV.isEmptyLine(line)) {
                line2data.put(i, line);
            }
        }
    }

    void refineDefine(Config define) {
        columns.forEach((n, col) -> {
            Field f = define.bean.fields.get(n);
            if (f == null) {
                f = new Field(define.bean, n, col.guessType());
                define.bean.fields.put(f.name, f);
            }
            col.updateDesc(f);
        });
    }

    void parse(Cfg cfg) {
        if (cfg != null) {
            makeOriginal(cfg.tbean.fields);
        }

        state = State.NORM;
        index = -1;
        for (String s : nameLine) {
            index++;
            if (s.isEmpty())
                continue;

            name = s;
            nameSep = Rules.trySep(name);

            switch (state) {
                case LIST:
                    onList();
                    break;
                case MAP:
                    onMap();
                    break;
                case MAYBELISTORMAP:
                    onMaybeListOrMap();
                    break;
                case MAYBEMAP:
                    onMaybeMap();
                    break;
                case MAYBEMAP2:
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

    private void makeOriginal(Map<String, Type> definedFields) {
        definedFields.forEach((k, t) -> {
            if (t instanceof TList) {
                TList type = (TList) t;
                if (type.count == 0) {
                    defined.put(k, new NameType(k, t));
                } else {
                    String columnName = (type.value instanceof TBean) ? k : Rules.parseListName(k);
                    Assert(null == defined.put(columnName, new NameType(k, t)), "list field name duplicate");
                }
            } else if (t instanceof TMap) {
                TMap type = (TMap) t;
                String columnName = (type.key instanceof TBean || type.value instanceof TBean) ? k : Rules.parseMapName(k).key;
                Assert(null == defined.put(columnName, new NameType(k, t)), "map field name duplicate");
            } else {
                defined.put(k, new NameType(k, t));
            }
        });
    }

    private boolean isDefined() {
        NameType t = defined.get(name);
        if (t != null) {
            Assert(t.type.columnSpan() == 1);
            return true;
        }
        return false;
    }

    private boolean isNameSepDefined() {
        if (nameSep.type != Rules.SepType.None) {
            NameType t = defined.get(nameSep.field);
            if (t != null) {
                if (nameSep.type == Rules.SepType.IntPostfix)
                    Assert(nameSep.num == 1);
                nameSepField = t;
                nameSepVisited = 1;
                return true;
            }
        }
        return false;
    }

    private boolean isNameSepDefinedProcessing() {
        if (nameSepField != null) {
            nameSepVisited++;
            if (nameSepVisited > nameSepField.type.columnSpan()) {
                nameSepField = null;
                return false;
            }
            return true;
        }
        return false;
    }

    private void onNorm() {
        if (isNameSepDefinedProcessing()) {
            add(nameSepField.name, index);
            return;
        }

        if (isDefined()) {
            put(name, index);
            return;
        }

        if (isNameSepDefined()) {
            put(nameSepField.name, index);
            return;
        }

        if (nameSep.type == Rules.SepType.IntPostfix && nameSep.num == 1) {
            A = nameSep.field;
            ABSpan = new ArrayList<>();
            ABSpan.add(index);
            state = State.MAYBELISTORMAP;
        } else {
            put(name, index);
        }
    }

    private void onMaybeListOrMap() {
        if (isDefined()) {
            put(A + "1", ABSpan.get(0));
            put(name, index);
            state = State.NORM;
            return;
        }

        if (isNameSepDefined()) {
            put(A + "1", ABSpan.get(0));
            put(nameSepField.name, index);
            state = State.NORM;
            return;
        }

        if (name.equals(A + "2")) {
            ABSpan.add(index);
            state = State.LIST;
        } else if (nameSep.type == Rules.SepType.IntPostfix && nameSep.num == 1) {
            B = nameSep.field;
            ABSpan.add(index);
            state = State.MAYBEMAP;
        } else {
            put(A + "1", ABSpan.get(0));
            state = State.NORM;
            onNorm();
        }
    }

    private void onList() {
        if (isDefined()) {
            put(Rules.makeListName(A), ABSpan);
            put(name, index);
            state = State.NORM;
            return;
        }

        if (isNameSepDefined()) {
            put(Rules.makeListName(A), ABSpan);
            put(nameSepField.name, index);
            state = State.NORM;
            return;
        }

        if (name.equals(A + (ABSpan.size() + 1))) {
            ABSpan.add(index);
        } else {
            put(Rules.makeListName(A), ABSpan);
            state = State.NORM;
            onNorm();
        }
    }

    private void onMaybeMap() {
        if (isDefined()) {
            put(A + "1", ABSpan.get(0));
            put(B + "1", ABSpan.get(1));
            put(name, index);
            state = State.NORM;
            return;
        }

        if (isNameSepDefined()) {
            put(A + "1", ABSpan.get(0));
            put(B + "1", ABSpan.get(1));
            put(nameSepField.name, index);
            state = State.NORM;
            return;
        }

        if (name.equals(A + "2")) {
            ABSpan.add(index);
            state = State.MAYBEMAP2;
        } else if (name.equals(B + "2")) {
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
            put(name, index);
            state = State.NORM;
            return;
        }

        if (isNameSepDefined()) {
            put(A + "1", ABSpan.get(0));
            put(B + "1", ABSpan.get(1));
            put(A + "2", ABSpan.get(2));
            put(nameSepField.name, index);
            state = State.NORM;
            return;
        }

        if (name.equals(B + "2")) {
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
            Assert(ABSpan.size() % 2 == 0);
            put(Rules.makeMapName(A, B), ABSpan);
            put(name, index);
            state = State.NORM;
            return;
        }

        if (isNameSepDefined()) {
            Assert(ABSpan.size() % 2 == 0);
            put(Rules.makeMapName(A, B), ABSpan);
            put(nameSepField.name, index);
            state = State.NORM;
            return;
        }

        if (ABSpan.size() % 2 == 0 && name.equals(A + (ABSpan.size() / 2 + 1))) {
            ABSpan.add(index);
        } else if (ABSpan.size() % 2 == 1 && name.equals(B + (ABSpan.size() / 2 + 1))) {
            ABSpan.add(index);
        } else {
            Assert(ABSpan.size() % 2 == 0);
            put(Rules.makeMapName(A, B), ABSpan);
            state = State.NORM;
            onNorm();
        }

    }

    private void onEnd() {
        switch (state) {
            case LIST:
                put(Rules.makeListName(A), ABSpan);
                break;
            case MAP:
                Assert(ABSpan.size() % 2 == 0);
                put(Rules.makeMapName(A, B), ABSpan);
                break;
            case MAYBELISTORMAP:
                put(A + "1", ABSpan.get(0));
                break;
            case MAYBEMAP:
                put(A + "1", ABSpan.get(0));
                put(B + "1", ABSpan.get(1));
                break;
            case MAYBEMAP2:
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
        Column col = new Column(this, s);
        col.indexs.addAll(a);
        col.descs.addAll(a.stream().map(descLine::get).collect(Collectors.toList()));
        Assert(null == columns.put(s, col), "field duplicate");
    }

    private void add(String s, int i) {
        Column col = columns.get(s);
        col.indexs.add(i);
        col.descs.add(descLine.get(i));
    }
}
