package configgen.data;

import configgen.Node;
import configgen.define.Field;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class Column extends Node {
    public final String name;
    public final List<Integer> indexs = new ArrayList<>();
    public final List<String> descs = new ArrayList<>();

    public Column(Data parent, String name) {
        super(parent, name);
        this.name = name;
    }

    String guessType() {
        if (indexs.size() > 1) {
            if (name.endsWith("List")) {
                return "list," + Rules.guessPrimitiveType(dataSet()) + "," + indexs.size();
            } else {
                Assert(indexs.size() % 2 == 0);
                Pair pair = dataKeyValueSet();
                return "map," + Rules.guessPrimitiveType(pair.key) + "," + Rules.guessPrimitiveType(pair.value) + "," + indexs.size() / 2;
            }
        } else {
            return Rules.guessPrimitiveTypeOrList(dataSet());
        }
    }

    void updateDesc(Field f){
        f.desc = descs.get(0);
        if (f.type.startsWith("map,")){
            f.desc += "," + descs.get(1);
        }
    }

    public List<String> dataList() {
        List<String> r = new ArrayList<>();
        for (List<String> row : ((Data) parent).line2data.values())
            r.addAll(indexs.stream().map(row::get).collect(Collectors.toList()));
        return r;
    }

    private Set<String> dataSet() {
        Set<String> r = new HashSet<>();
        for (List<String> row : ((Data) parent).line2data.values())
            r.addAll(indexs.stream().map(row::get).collect(Collectors.toList()));
        return r;
    }

    private static class Pair {
        Set<String> key = new HashSet<>();
        Set<String> value = new HashSet<>();
    }

    private Pair dataKeyValueSet() {
        Pair res = new Pair();
        for (List<String> row : ((Data) parent).line2data.values()) {
            int i = 0;
            for (int index : indexs) {
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