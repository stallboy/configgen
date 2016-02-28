package configgen.type;

import configgen.define.Range;

import java.util.ArrayList;
import java.util.List;

public class Constraint {
    public final List<SRef> references = new ArrayList<>();
    public Range range;
}
