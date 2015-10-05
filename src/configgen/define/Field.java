package configgen.define;

import configgen.*;
import org.w3c.dom.Element;

public class Field extends Node {
    public String desc;
    public final String name;
    public final String type;
    public final String ref;
    public final String nullableRef;
    public final String keyRef;
    public final String listRef;
    public final String listRefKey;
    public final String range;
    public final String own;

    public Field(Bean parent, Element self) {
        super(parent, "");
        String[] attrs = DomUtils.attributes(self, "desc", "name", "type",
                "ref", "nullableref", "keyref", "listref", "range", "own");
        desc = attrs[0];
        name = attrs[1];
        link = name;
        type = attrs[2];

        ref = attrs[3];
        nullableRef = attrs[4];
        keyRef = attrs[5];
        String r = attrs[6].trim();
        if (r.isEmpty()) {
            listRef = "";
            listRefKey = "";
        } else {
            String[] sp = r.split(",");
            listRef = sp[0];
            listRefKey = sp[1];
        }
        range = attrs[7];
        own = attrs[8];
    }

    public Field(Bean parent, String name, String type) {
        super(parent, "");
        this.name = name;
        this.type = type;
        link = name;

        ref = "";
        nullableRef = "";
        keyRef = "";
        listRef = "";
        listRefKey = "";
        range = "";
        own = "";
    }

    public void save(Element parent) {
        Element self = DomUtils.newChild(parent, "field");
        if (!desc.isEmpty())
            self.setAttribute("desc", desc);
        self.setAttribute("name", name);
        self.setAttribute("type", type);

        if (!ref.isEmpty())
            self.setAttribute("ref", ref);
        else if (!nullableRef.isEmpty())
            self.setAttribute("nullableref", nullableRef);
        if (!keyRef.isEmpty())
            self.setAttribute("keyref", keyRef);
        if (!listRef.isEmpty())
            self.setAttribute("listref", listRef + "," + listRefKey);

        if (!range.isEmpty())
            self.setAttribute("range", range);

        if (!own.isEmpty())
            self.setAttribute("own", own);
    }
}