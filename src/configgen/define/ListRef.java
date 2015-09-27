package configgen.define;

import configgen.Node;
import configgen.Utils;
import org.w3c.dom.Element;

public class ListRef extends Node {
    public final String name;
    public final String[] keys;
    public final String ref;
    public final String[] refKeys;

    public ListRef(Bean parent, Element self) {
        super(parent, "");
        String[] attrs = Utils.attributes(self, "name", "keys", "ref", "refkeys");
        name = attrs[0];
        link = name;
        String r = attrs[1].trim();
        if (!r.isEmpty())
            keys = r.split(",");
        else
            keys = new String[0];
        ref = attrs[2];
        r = attrs[3].trim();
        if (!r.isEmpty())
            refKeys = r.split(",");
        else
            refKeys = new String[0];
    }

    public void save(Element parent) {
        Element self = Utils.newChild(parent, "listref");
        self.setAttribute("name", name);
        self.setAttribute("keys", String.join(",", keys));
        self.setAttribute("ref", ref);
        self.setAttribute("refkeys", String.join(",", refKeys));
    }
}
