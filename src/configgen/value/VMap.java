package configgen.value;

import configgen.type.TMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VMap extends VComposite {
    public final Map<Value, Value> map = new LinkedHashMap<>();

    VMap(TMap type, List<Cell> data) {
        super(type, data);

        require(data.size() == type.columnSpan(), "数据和类型占格数不匹配");
        int kc = type.key.columnSpan();
        int vc = type.value.columnSpan();
        for (int i = 0; i < type.count; i++) {
            int s = i * (kc + vc);
            if (!data.get(s).data.isEmpty()) {
                Value key = Value.create(type.key, data.subList(s, s + kc));
                Value value = Value.create(type.value, data.subList(s + kc, s + kc + vc));
                require(null == map.put(key, value), "字典key重复");
            } else {
                for (Cell dc : data.subList(s, s + kc + vc)) {
                    require(dc.data.trim().isEmpty(), "字典遇到entry空格后，之后也必须都是空格", dc.toString());
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
