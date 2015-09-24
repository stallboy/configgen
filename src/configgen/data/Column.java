package configgen.data;

import configgen.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Column extends Node {
    public final String name;
    public final List<Integer> indexs = new ArrayList<>();
    public final List<String> descs = new ArrayList<>();

    public Column(Data parent, String name) {
        super(parent, name);
        this.name = name;
    }

    String guessType() {
        return "string"; //todo
    }

}