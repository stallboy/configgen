package configgen.value;

import configgen.CSV;
import configgen.Node;
import configgen.type.TLong;

import java.util.List;

public class VLong extends Value {
    public final TLong type;
    public final Cell raw;
    public long value;

    public VLong(Node parent, String link, TLong type, List<Cell> data) {
        super(parent, link);
        this.type = type;
        Assert(data.size() == 1);
        raw = data.get(0);
        try {
            value = CSV.parseLong(raw.data);
        } catch (Exception e) {
            Assert(false, e.toString(), raw.toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof VLong && value == ((VLong) o).value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

}
