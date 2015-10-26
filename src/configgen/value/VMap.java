package configgen.value;

import configgen.Node;
import configgen.type.TMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VMap extends Value {
    public final Map<Value, Value> map = new LinkedHashMap<>();

    public VMap(Node parent, String name, TMap type, List<Cell> data) {
        super(parent, name, type, data);

        require(data.size() == type.columnSpan());
        int kc = type.key.columnSpan();
        int vc = type.value.columnSpan();
        for (int i = 0, idx = 0; i < type.count; i++) {
            int s = i * (kc + vc);
            if (!data.get(s).data.isEmpty()) {
                Value key = Value.create(this, "key" + idx, type.key, data.subList(s, s + kc));
                Value value = Value.create(this, "value" + idx, type.value, data.subList(s + kc, s + kc + vc));
                require(null == map.put(key, value), "map key duplicate", toString());
                idx++;
            } else {
                for (Cell dc : data.subList(s, s + kc + vc)) {
                    require(dc.data.trim().isEmpty(), "map entry ignore by first cell empty, but part filled", dc.toString());
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
        return o != null && o instanceof VMap && map.equals(((VMap) o).map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

}
