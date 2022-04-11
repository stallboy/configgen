package configgen.value;

import configgen.type.TMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VMap extends VComposite {
    private final Map<Value, Value> map = new LinkedHashMap<>();

    VMap(TMap type, AData<TMap> adata) {
        super(type, adata.cells);

        List<Cell> parsed;
        if (adata.packAsOne) { //为简单起见，TMap自身不支持配置packSep,pack，但被上层用pack时还是支持了.
            require(adata.cells.size() == 1);
            Cell dat = adata.cells.get(0);
            parsed = Cells.parseNestList(dat);

        } else if (type.isPackByBlock) {
            parsed = VTable.parseBlock(adata.cells);

        } else {
            require(adata.cells.size() == adata.fullType.columnSpan(), "数据和类型占格数不匹配");
            parsed = adata.cells;
        }

        int kc = adata.packAsOne ? 1 : adata.fullType.key.columnSpan();
        int vc = adata.packAsOne ? 1 : adata.fullType.value.columnSpan();
        for (int s = 0; s < parsed.size(); s += kc + vc) {
            if (!parsed.get(s).getData().trim().isEmpty()) { //第一个单元作为是否还有key-value对的标记
                Value key = Values.create(type.key, parsed.subList(s, s + kc),
                        adata.fullType.key, adata.packAsOne);
                Value value = Values.create(type.value, parsed.subList(s + kc, s + kc + vc),
                        adata.fullType.value, adata.packAsOne);
                Value old = map.put(key, value);

                require(null == old, "字典key重复");
            } else {
                for (Cell dc : parsed.subList(s, s + kc + vc)) {
                    require(dc.getData().trim().isEmpty(), "map的entry第一个为空格后，之后也必须都是空格", dc);
                }
            }
        }
    }

    public Map<Value, Value> getMap() {
        return map;
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
