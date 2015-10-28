package configgen.value;

import configgen.Node;
import configgen.type.Range;
import configgen.type.TPrimitive;

import java.util.List;

public abstract class VPrimitive extends Value {
    final Cell raw;

    VPrimitive(Node parent, String name, TPrimitive type, List<Cell> data) {
        super(parent, name, type, data);
        require(data.size() == 1);
        raw = data.get(0);
    }

    public void verifyConstraint() {
        verifyRefs();

        Range range = type.constraint.range;
        if (range != null) {
            require(checkRange(range), "range err", toString());
        }
    }

    public abstract boolean checkRange(Range range);
}
