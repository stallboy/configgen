package configgen.value;

import configgen.type.TMap;
import configgen.util.NestListParser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VMap extends VComposite {
    public final Map<Value, Value> map = new LinkedHashMap<>();

    VMap(TMap type, List<Cell> data, boolean compressAsOne) {
        super(type, data);

        List<Cell> parsed;
        if (compressAsOne) {
            require(data.size() == 1);
            Cell dat = data.get(0);
            parsed = NestListParser.parseNestList(dat.data).stream().map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
        } else {
            require(data.size() == type.columnSpan(), "数据和类型占格数不匹配");
            parsed = data;
        }

        int kc = compressAsOne ? 1 : type.key.columnSpan();
        int vc = compressAsOne ? 1 : type.value.columnSpan();
        for (int s = 0; s < parsed.size(); s += kc + vc) {
            if (!parsed.get(s).data.isEmpty()) {
                Value key = Value.create(type.key, parsed.subList(s, s + kc), compressAsOne);
                Value value = Value.create(type.value, parsed.subList(s + kc, s + kc + vc), compressAsOne);
                require(null == map.put(key, value), "字典key重复");
            } else {
                for (Cell dc : parsed.subList(s, s + kc + vc)) {
                    require(dc.data.trim().isEmpty(), "map的entry第一个为空格后，之后也必须都是空格", dc);
                }
            }
        }
    }

    @Override
    public void accept(ValueVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void verifyConstraint() {
        map.forEach((k, v) -> {
            k.verifyConstraint();
            v.verifyConstraint();
        });
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof VMap && map.equals(((VMap) o).map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

}
