package configgen.define;

import configgen.Node;
import org.w3c.dom.Element;

public class UniqueKey extends Node {
    public final String[] keys;

    public UniqueKey(Table parent, Element self) {
        super(parent, "uniqueKey");
        keys = DomUtils.parseStringArray(self, "keys");
    }

    UniqueKey(Table _parent, UniqueKey original) {
        super(_parent, original.name);
        keys = original.keys;
    }

    @Override
    public String toString() {
        return String.join(",", keys);
    }

    void save(Element parent) {
        DomUtils.newChild(parent, "uniqueKey").setAttribute("keys", this.toString());
    }
}