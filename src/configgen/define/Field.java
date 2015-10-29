package configgen.define;

import configgen.Node;
import org.w3c.dom.Element;

public class Field extends Node {
    public String desc;
    public final String type;
    public String ref;
    public String nullableRef;
    public String keyRef;
    public String listRef;
    public String listRefKey;
    public final String range;
    public final String own;

    public Field(Bean parent, Element self) {
        super(parent, self.getAttribute("name"));
        String[] attrs = DomUtils.attributes(self, "desc", "name", "type",
                "ref", "nullableref", "keyref", "listref", "range", "own");
        desc = attrs[0];
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
        super(parent, name);
        this.type = type;

        ref = "";
        nullableRef = "";
        keyRef = "";
        listRef = "";
        listRefKey = "";
        range = "";
        own = "";
    }

    Field(Bean _parent, Field original) {
        super(_parent, original.name);
        desc = original.desc;
        type = original.type;

        ref = original.ref;
        nullableRef = original.nullableRef;
        keyRef = original.keyRef;
        listRef = original.listRef;
        listRefKey = original.listRefKey;
        range = original.range;
        own = original.own;
    }

    Field extract(Bean _parent, String _own) {
        if (own.contains(_own))
            return new Field(_parent, this);
        return null;
    }

    void resolveExtract() {
        if (!ref.isEmpty() && !((ConfigCollection) root).configs.containsKey(ref))
            ref = "";

        if (!nullableRef.isEmpty() && !((ConfigCollection) root).configs.containsKey(nullableRef))
            nullableRef = "";

        if (!keyRef.isEmpty() && !((ConfigCollection) root).configs.containsKey(keyRef))
            keyRef = "";

        if (!listRef.isEmpty()) {
            Config cfg = ((ConfigCollection) root).configs.get(listRef);
            if (cfg == null) {
                listRef = "";
                listRefKey = "";
            } else {
                if (!cfg.bean.fields.containsKey(listRefKey)) {
                    listRef = "";
                    listRefKey = "";
                }
            }
        }
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