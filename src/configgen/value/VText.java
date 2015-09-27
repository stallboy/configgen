package configgen.value;

import configgen.Node;
import configgen.type.Range;
import configgen.type.TText;

import java.util.List;

public class VText extends VPrimitive {
    public String value;

    public VText(Node parent, String link, TText type, List<Cell> data) {
        super(parent, link, type, data);
        value = raw.data;
    }

    @Override
    public boolean checkRange(Range range) {
        return false; //not going to happen
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof VString && value.equals(((VString) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
