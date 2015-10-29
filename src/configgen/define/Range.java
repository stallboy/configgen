package configgen.define;

import configgen.Node;
import org.w3c.dom.Element;

public class Range extends Node {
    public final String key;
    public final int min;
    public final int max;

    public Range(Bean parent, Element self) {
        super(parent, self.getAttribute("key"));
        String[] attr = DomUtils.attributes(self, "key", "min", "max");
        key = attr[0];
        min = Integer.decode(attr[1]);
        max = Integer.decode(attr[2]);
        require(max >= min, attr[1]);
    }

    Range(Bean _parent, Range original) {
        super(_parent, original.name);
        key = original.key;
        min = original.min;
        max = original.max;
    }

    public void save(Element parent) {
        Element self = DomUtils.newChild(parent, "range");
        self.setAttribute("key", key);
        self.setAttribute("min", String.valueOf(min));
        self.setAttribute("max", String.valueOf(max));
    }
}
