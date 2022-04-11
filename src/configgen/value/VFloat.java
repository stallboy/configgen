package configgen.value;

import configgen.define.Range;
import configgen.type.TFloat;
import configgen.util.PrimitiveParser;

import java.util.List;

public class VFloat extends VPrimitive {
    public final float value;

    VFloat(TFloat type, List<Cell> data) {
        super(type, data);
        float v = 0;
        try {
            v = PrimitiveParser.parseFloat(raw.getData());
        } catch (Exception e) {
            error(e);
        }
        value = v;
    }

    @Override
    public boolean checkRange(Range range) {
        return value >= range.min && value <= range.max;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof VFloat && value == ((VFloat) o).value;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(value);
    }

    @Override
    public void accept(ValueVisitor visitor) {
        visitor.visit(this);
    }
}
