package configgen.define;

import configgen.Node;
import org.w3c.dom.Element;

public class Column extends Node {
    public String desc;
    public final String type;
    public final String own;

    public ForeignKey foreignKey;
    public KeyRange keyRange;

    public Column(Bean _parent, Element self) {
        super(_parent, self.getAttribute("name"));
        DomUtils.permitAttributes(self, "desc", "name", "type", "own",
                "ref", "refType", "keyRef", "range");
        desc = self.getAttribute("desc");
        type = self.getAttribute("type");
        own = self.getAttribute("own");

        if (self.hasAttribute("ref"))
            foreignKey = new ForeignKey(this, self);
        if (self.hasAttribute("range"))
            keyRange = new KeyRange(this, self);
    }

    Column(Bean _parent, String _name, String type, String desc) {
        super(_parent, _name);
        this.type = type;
        this.desc = desc;
        this.own = "";
    }

    Column(Bean _parent, Column original) {
        super(_parent, original.name);
        desc = original.desc;
        type = original.type;
        own = original.own;
        foreignKey = new ForeignKey(this, original.foreignKey);
        keyRange = new KeyRange(this, original.keyRange);
    }

    Column extract(Bean _parent, String _own) {
        if (own.contains(_own))
            return new Column(_parent, this);
        return null;
    }

    void resolveExtract() {
        if (foreignKey.invalid())
            foreignKey = null;
    }

    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "column");
        self.setAttribute("name", name);
        self.setAttribute("type", type);
        if (!desc.isEmpty())
            self.setAttribute("desc", desc);
        if (!own.isEmpty())
            self.setAttribute("own", own);

        if (foreignKey != null)
            foreignKey.update(self);
        if (keyRange != null)
            keyRange.update(self);
    }
}