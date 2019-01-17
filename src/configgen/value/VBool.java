package configgen.value;

import configgen.define.Range;
import configgen.type.TBool;
import configgen.util.CSVParser;

import java.util.List;

public class VBool extends VPrimitive {
    public final boolean value;

    VBool(TBool type, List<Cell> data) {
        super(type, data);
        String s = raw.data.trim();
        value = CSVParser.parseBoolean(s); //s.equalsIgnoreCase("true") || s.equals("1");
        require(s.isEmpty() || s.equalsIgnoreCase("false") || s.equals("0") || value, "不是布尔值");
    }

    @Override
    public boolean checkRange(Range range) {
        return false; //not going to happen
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof VBool && value == ((VBool) o).value;
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
