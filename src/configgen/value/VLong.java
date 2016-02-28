package configgen.value;

import configgen.Node;
import configgen.data.CSV;
import configgen.define.Range;
import configgen.type.TLong;

import java.util.List;

public class VLong extends VPrimitive {
    public final long value;

    public VLong(Node parent, String name, TLong type, List<Cell> data) {
        super(parent, name, type, data);
        long v = 0;
        try {
            v = CSV.parseLong(raw.data);
        } catch (Exception e) {
            require(false, e.toString(), raw.toString());
        }
        value = v;
    }

    @Override
    public boolean checkRange(Range range) {
        return value >= range.min && value <= range.max;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof VLong && value == ((VLong) o).value;
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
