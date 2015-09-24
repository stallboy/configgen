package configgen.type;

import java.util.ArrayList;
import java.util.List;

public class Constraint {
    public final List<Cfg> refs = new ArrayList<>();
    public final List<Cfg> nullableRefs = new ArrayList<>();
    public final List<Cfg> keyRefs = new ArrayList<>();
    public Range range;
}
