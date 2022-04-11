package configgen.value;

import configgen.define.Range;
import configgen.type.TLong;
import configgen.util.PrimitiveParser;

import java.util.List;

public class VLong extends VPrimitive {
    public final long value;

    VLong(TLong type, List<Cell> data) {
        super(type, data);
        long v = 0;
        try {
            v = PrimitiveParser.parseLong(raw.getData());
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
        return o instanceof VLong && value == ((VLong) o).value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public void accept(ValueVisitor visitor) {
        visitor.visit(this);
    }
}
