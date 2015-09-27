package configgen.define;

import configgen.CSV;
import configgen.Node;
import configgen.Utils;
import org.w3c.dom.Element;

public class Ref extends Node {
    public final String name;
    public final String[] keys;
    public final String ref;
    public final boolean nullable;
    public final String keyRef;

    public Ref(Bean parent, Element self) {
        super(parent, "");
        String[] attrs = Utils.attributes(self, "name", "keys", "ref", "nullable", "keyref");
        name = attrs[0];
        link = name;
        String r = attrs[1].trim();
        if (!r.isEmpty())
            keys = r.split(",");
        else
            keys = new String[0];
        ref = attrs[2];
        nullable = CSV.parseBoolean(attrs[3]);
        keyRef = attrs[4];
    }

    public void save(Element parent) {
        Element self = Utils.newChild(parent, "ref");
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
