package configgen.define;

import configgen.Node;
import org.w3c.dom.Element;

public class Range extends Node {
    public final String key;
    public final int min;
    public final int max;

    public Range(Bean parent, Element self) {
        super(parent, "");
        String[] attr = DomUtils.attributes(self, "key", "min", "max");
        key = attr[0];
        link = key;
        min = Integer.decode(attr[1]);
        max = Integer.decode(attr[2]);

        Assert(max >= min, attr[1]);
    }

    public void save(Element parent) {
        Element self = DomUtils.newChild(parent, "range");
        self.setAttribute("key", key);
        self.setAttribute("min", String.valueOf(min));
        self.setAttribute("max", String.valueOf(max));
    }
}
