package configgen.value;

import configgen.define.Range;
import configgen.type.TInt;
import configgen.util.CSV;

import java.util.List;

public class VInt extends VPrimitive {
    public final int value;

    VInt(TInt type, List<Cell> data) {
        super(type, data);
        int v = 0;
        try {
            v = CSV.parseInt(raw.data);
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
        return o instanceof VInt && value == ((VInt) o).value;
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
