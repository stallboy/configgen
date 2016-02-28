package configgen.data;

import configgen.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DColumn extends Node {
    public final List<Integer> indexes = new ArrayList<>();
    public final List<String> descs = new ArrayList<>();

    public DColumn(DTable parent, String name) {
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

    public List<String> dataList() {
        List<String> r = new ArrayList<>();
        for (List<String> row : ((DTable) parent).line2data.values())
            r.addAll(indexes.stream().map(row::get).collect(Collectors.toList()));
        return r;
    }

    private Set<String> dataSet() {
        Set<String> r = new HashSet<>();
        for (List<String> row : ((DTable) parent).line2data.values())
            r.addAll(indexes.stream().map(row::get).collect(Collectors.toList()));
        return r;
    }

    private static class Pair {
        final Set<String> key = new HashSet<>();
        final Set<String> value = new HashSet<>();
    }

    private Pair dataKeyValueSet() {
        Pair res = new Pair();
        for (List<String> row : ((DTable) parent).line2data.values()) {
            int i = 0;
            for (int index : indexes) {
                String r = row.get(index);
                if (i % 2 == 0)
                    res.key.add(r);
                else
                    res.value.add(r);
                i++;
            }
        }
        return res;
    }

}