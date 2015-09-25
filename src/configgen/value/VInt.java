package configgen.value;

import configgen.CSV;
import configgen.Node;
import configgen.type.TInt;

import java.util.List;

public class VInt extends Value {
    public final TInt type;
    public final Cell raw;
    public int value;

    public VInt(Node parent, String link, TInt type, List<Cell> data) {
        super(parent, link);
        this.type = type;
        Assert(data.size() == 1);
        raw = data.get(0);
        try {
            value = CSV.parseInt(raw.data);
        }catch (Exception e){
            Assert(false, e.toString(), raw.toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof VInt && value == ((VInt) o).value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
