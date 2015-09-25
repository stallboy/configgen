package configgen.value;

import configgen.Node;
import configgen.type.TText;

import java.util.List;

public class VText extends Value {
    public final TText type;
    public final Cell raw;
    public String value;

    public VText(Node parent, String link, TText type, List<Cell> data) {
        super(parent, link);
        this.type = type;
        Assert(data.size() == 1);
        raw = data.get(0);
        value = raw.data;
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
