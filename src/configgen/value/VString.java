package configgen.value;

import configgen.Node;
import configgen.type.Range;
import configgen.type.TString;

import java.util.List;

public class VString extends VPrimitive {
    public String value;

    public VString(Node parent, String link, TString type, List<Cell> data) {
        super(parent, link, type, data);
        value = raw.data;
    }

    @Override
    public boolean checkRange(Range range) {
        int len = value.length();
        return len >= range.min && len <= range.max;
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
