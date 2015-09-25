package configgen.value;

import configgen.CSV;
import configgen.Node;
import configgen.type.Range;
import configgen.type.TFloat;

import java.util.List;

public class VFloat extends VPrimitive {
    public float value;

    public VFloat(Node parent, String link, TFloat type, List<Cell> data) {
        super(parent, link, type, data);
        try {
            value = CSV.parseFloat(raw.data);
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
        return o != null && o instanceof VFloat && value == ((VFloat) o).value;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(value);
    }
}
