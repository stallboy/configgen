package configgen.value;

import configgen.Node;
import configgen.type.TBool;

import java.util.List;

public class VBool extends Value {
    public final TBool type;
    public final Cell raw;
    public final boolean value;

    public VBool(Node parent, String link, TBool type, List<Cell> data) {
        super(parent, link);
        this.type = type;
        Assert(data.size() == 1);
        raw = data.get(0);
        String s = raw.data.trim();
        value = s.equalsIgnoreCase("true") || s.equals("1");
        Assert(s.isEmpty() || s.equalsIgnoreCase("false") || s.equals("0") || value, "not bool", raw.toString());
    }


    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof VBool && value == ((VBool) o).value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

}
