package configgen.value;

import configgen.Node;
import configgen.type.Range;
import configgen.type.TBool;

import java.util.List;

public class VBool extends VPrimitive {
    public final boolean value;

    public VBool(Node parent, String link, TBool type, List<Cell> data) {
        super(parent, link, type, data);
        String s = raw.data.trim();
        value = s.equalsIgnoreCase("true") || s.equals("1");
        Assert(s.isEmpty() || s.equalsIgnoreCase("false") || s.equals("0") || value, "not bool", raw.toString());
    }

    @Override
    public boolean checkRange(Range range) {
        return false; //not going to happen
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof VBool && value == ((VBool) o).value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    @Override
    public void accept(ValueVisitor visitor) {
        visitor.visit(this);
    }
}
