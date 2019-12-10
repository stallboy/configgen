package configgen.value;

import configgen.type.TMap;
import configgen.util.NestListParser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VMap extends VComposite {
    private final Map<Value, Value> map = new LinkedHashMap<>();

    VMap(TMap type, AData<TMap> adata) {
        super(type, adata.cells);

        List<Cell> parsed;
        if (adata.isCompressAsOne()) {
            require(adata.cells.size() == 1);
            Cell dat = adata.cells.get(0);
            parsed = Cells.parseNestList(dat);

        } else {
            require(adata.cells.size() == adata.fullType.columnSpan(), "数据和类型占格数不匹配");
            parsed = adata.cells;
        }

        int kc = adata.isCompressAsOne() ? 1 : adata.fullType.key.columnSpan();
        int vc = adata.isCompressAsOne() ? 1 : adata.fullType.value.columnSpan();
        for (int s = 0; s < parsed.size(); s += kc + vc) {
            if (!parsed.get(s).data.isEmpty()) { //第一个单元作为是否还有key-value对的标记
                AData<?> keyAData = new AData<>(parsed.subList(s, s + kc), adata.fullType.key, adata.isCompressAsOne());
                Value key = Values.create(type.key, keyAData);

                AData<?> valueAData = new AData<>(parsed.subList(s + kc, s + kc + vc), adata.fullType.value, adata.isCompressAsOne());
                Value value = Values.create(type.value, valueAData);
                Value old = map.put(key, value);

                require(null == old, "字典key重复");
            } else {
                for (Cell dc : parsed.subList(s, s + kc + vc)) {
                    require(dc.data.trim().isEmpty(), "map的entry第一个为空格后，之后也必须都是空格", dc);
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
