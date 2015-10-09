package configgen.value;

import configgen.CSV;
import configgen.Node;
import configgen.type.Range;
import configgen.type.TInt;

import java.util.List;

public class VInt extends VPrimitive {
    public final int value;

    public VInt(Node parent, String link, TInt type, List<Cell> data) {
        super(parent, link, type, data);
        int v = 0;
        try {
            v = CSV.parseInt(raw.data);
        } catch (Exception e) {
            Assert(false, e.toString(), raw.toString());
        }
        value = v;
    }

    @Override
    public boolean checkRange(Range range) {
        return value >= range.min && value <= range.max;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof VInt && value == ((VInt) o).value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public void accept(ValueVisitor visitor) {
        visitor.visit(this);
    }
}
