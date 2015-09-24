package configgen.define;

import configgen.Node;
import configgen.Utils;
import org.w3c.dom.Element;

public class ListRef extends Node {
    public final String name;
    public final String[] keys;
    public final String ref;
    public final String[] refFields;

    public ListRef(Bean parent, Element self) {
        super(parent, "");
        String[] attrs = Utils.attrs(self, "name", "keys", "ref", "refkeys");
        name = attrs[0];
        link = "[listref]" + name;
        keys = attrs[1].split(",");
        ref = attrs[2];
        refFields = attrs[3].split(",");
    }
}
