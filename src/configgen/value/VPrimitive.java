package configgen.value;

import configgen.define.Range;
import configgen.type.TPrimitive;

import java.util.List;

public abstract class VPrimitive extends Value {
    public final Cell raw;

    VPrimitive(TPrimitive type, List<Cell> data) {
        super(type);
        require(data.size() == 1);
        raw = data.get(0);
    }

    public abstract boolean checkRange(Range range);

    @Override
    public void verifyConstraint() {
        verifyRefs();
        if (type.constraint.range != null) {
            require(checkRange(type.constraint.range), "取值范围错误，范围是", type.constraint.range, "值是", raw);
        }
    }

    @Override
    public boolean isCellEmpty() {
        return raw.data.trim().isEmpty();
    }

    @Override
    public void collectCells(List<Cell> targetCells) {
        targetCells.add(raw);
    }

    @Override
    public String toString() {
        return raw.toString();
    }
}
