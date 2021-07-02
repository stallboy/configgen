package configgen.data;

import configgen.Node;
import configgen.type.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class DColumn extends Node {
    final List<Integer> indexes = new ArrayList<>();
    final List<String> descs = new ArrayList<>();

    private Type columnType;

    public Type getColumnType() {
        return columnType;
    }

    void setColumnType(Type columnType) {
        this.columnType = columnType;
    }

    DColumn(DTable parent, String name) {
        super(parent, name);
    }

    String guessType() {
        if (indexes.size() > 1) {
            if (name.endsWith("List")) {
                return "list," + GuessHelper.guessPrimitiveType(dataSet()) + "," + indexes.size();
            } else {
                require(indexes.size() % 2 == 0);
                Pair pair = dataKeyValueSet();
                return "map," + GuessHelper.guessPrimitiveType(pair.key) + "," + GuessHelper.guessPrimitiveType(pair.value) + "," + indexes.size() / 2;
            }
        } else {
            return GuessHelper.guessPrimitiveTypeOrList(dataSet());
        }
    }

    String desc() {
        return descs.get(0);
    }

    private Set<String> dataSet() {
        Set<String> r = new HashSet<>();
        for (DSheet sheet : ((DTable) parent).getSheets()) {
            for (List<String> record : sheet.getRecordList()) {
                if (record.isEmpty()) {
                    continue;
                }

                for (Integer index : indexes) {
                    r.add(record.get(index));
                }
            }
        }
        return r;
    }


    private static class Pair {
        final Set<String> key = new HashSet<>();
        final Set<String> value = new HashSet<>();
    }

    private Pair dataKeyValueSet() {
        Pair res = new Pair();
        for (DSheet sheet : ((DTable) parent).getSheets()) {
            for (List<String> record : sheet.getRecordList()) {
                if (record.isEmpty()) {
                    continue;
                }

                int i = 0;
                for (Integer index : indexes) {
                    String r = record.get(index);
                    if (i % 2 == 0)
                        res.key.add(r);
                    else
                        res.value.add(r);
                    i++;
                }
            }
        }
        return res;
    }

}