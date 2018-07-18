package configgen.define;

import configgen.Node;
import configgen.util.DomUtils;
import org.w3c.dom.Element;

public class KeyRange extends Node {
    public final String key;
    public final Range range;

    public KeyRange(Bean _parent, Element self) {
        super(_parent, "keyRange");
        DomUtils.permitAttributes(self, "key", "min", "max");
        key = self.getAttribute("key");
        int min = Integer.decode(self.getAttribute("min"));
        int max = Integer.decode(self.getAttribute("max"));
        require(max >= min, key);
        range = new Range(min, max);
    }

    public KeyRange(Column _parent, Element self) {
        super(_parent, "range");
        key = self.getAttribute("name");
        String[] sp = self.getAttribute("range").split(",");
        int min = Integer.decode(sp[0]);
        int max = Integer.decode(sp[1]);
        require(max >= min, key);
        range = new Range(min, max);
    }

    KeyRange(Node _parent, KeyRange original) {
        super(_parent, original.name);
        key = original.key;
        range = original.range;
    }

    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "keyRange");
        self.setAttribute("key", key);
        self.setAttribute("min", String.valueOf(range.min));
        self.setAttribute("max", String.valueOf(range.max));
    }

    void update(Element self) {
        self.setAttribute("range", range.toString());
    }
}
