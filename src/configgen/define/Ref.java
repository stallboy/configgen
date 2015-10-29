package configgen.define;

import configgen.Node;
import configgen.data.CSV;
import org.w3c.dom.Element;

public class Ref extends Node {
    public final String[] keys;
    public final String ref;
    public final boolean nullable;
    public final String keyRef;

    public Ref(Bean parent, Element self) {
        super(parent, self.getAttribute("name"));
        String[] attrs = DomUtils.attributes(self, "name", "keys", "ref", "nullable", "keyref");
        String r = attrs[1].trim();
        if (!r.isEmpty())
            keys = r.split(",");
        else
            keys = new String[0];
        ref = attrs[2];
        nullable = CSV.parseBoolean(attrs[3]);
        keyRef = attrs[4];
    }

    Ref(Bean _parent, Ref original) {
        super(_parent, original.name);
        keys = original.keys.clone();
        ref = original.ref;
        nullable = original.nullable;
        keyRef = original.keyRef;
    }

    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "ref");
        self.setAttribute("name", name);
        self.setAttribute("keys", String.join(",", keys));
        if (!ref.isEmpty())
            self.setAttribute("ref", ref);
        if (nullable)
            self.setAttribute("nullable", "true");
        if (!keyRef.isEmpty())
            self.setAttribute("keyref", keyRef);
    }
}
