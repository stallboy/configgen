package configgen.value;

import configgen.CSV;
import configgen.Node;
import configgen.type.TFloat;

import java.util.List;

public class VFloat extends Value {
    public final TFloat type;
    public final Cell raw;
    public float value;

    public VFloat(Node parent, String link, TFloat type, List<Cell> data) {
        super(parent, link);
        this.type = type;
        Assert(data.size() == 1);
        raw = data.get(0);
        try {
            value = CSV.parseFloat(raw.data);
        } catch (Exception e) {
            Assert(false, e.toString(), raw.toString());
        }
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
