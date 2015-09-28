package configgen.value;

import configgen.Node;
import configgen.type.Range;
import configgen.type.TPrimitive;

import java.util.List;

public abstract class VPrimitive extends Value {
    public final Cell raw;

    public VPrimitive(Node parent, String link, TPrimitive type, List<Cell> data) {
        super(parent, link, type, data);
        this.type = type;
        Assert(data.size() == 1);
        raw = data.get(0);
    }

    public void verifyConstraint() {
        verifyRefs();

        Range range = type.constraint.range;
        if (range != null) {
            Assert(checkRange(range), "range err", toString());
        }
    }

    public abstract boolean checkRange(Range range);
}
