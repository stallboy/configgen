package configgen.value;

import configgen.CSV;
import configgen.Node;
import configgen.type.Range;
import configgen.type.TLong;

import java.util.List;

public class VLong extends VPrimitive {
    public long value;

    public VLong(Node parent, String link, TLong type, List<Cell> data) {
        super(parent, link, type, data);
        try {
            value = CSV.parseLong(raw.data);
        } catch (Exception e) {
            Assert(false, e.toString(), raw.toString());
        }
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
