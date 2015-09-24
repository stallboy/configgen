package configgen.define;

import configgen.Node;
import configgen.Utils;
import org.w3c.dom.Element;

public class Range extends Node {
    public final String key;
    public final int min;
    public final int max;

    public Range(Bean parent, Element self) {
        super(parent, "");
        String[] attr = Utils.attrs(self, "key", "range");
        key = attr[0];
        link = "[range]" + key;
        String[] sp = attr[1].split(",");
        min = Integer.decode(sp[0]);
        max = Integer.decode(sp[1]);

        Assert(max >= min, attr[1]);
    }
}
