package configgen.define;

import configgen.Node;
import configgen.util.DomUtils;
import org.w3c.dom.Element;

public class Column extends Node {
    public String desc;
    public final String type;
    private final String own;
    public final boolean compress;
    public final char compressSeparator;

    public ForeignKey foreignKey;
    public KeyRange keyRange;

    Column(Bean _parent, Element self) {
        super(_parent, self.getAttribute("name"));
        DomUtils.permitAttributes(self, "desc", "name", "type", "own",
                "ref", "refType", "keyRef", "range", "compress");
        desc = self.getAttribute("desc");
        type = self.getAttribute("type");
        own = self.getAttribute("own");

        if (self.hasAttribute("ref"))
            foreignKey = new ForeignKey(this, self);
        if (self.hasAttribute("range"))
            keyRange = new KeyRange(this, self);

        compress = self.hasAttribute("compress");
        if (compress) {
            String sep = self.getAttribute("compress");
            require(sep.length() == 1, "compress separator length not 1, separator=" + sep);
            compressSeparator = sep.toCharArray()[0];
        } else {
            compressSeparator = ';';
        }
    }

    Column(Bean _parent, String _name, String type, String desc) {
        super(_parent, _name);
        this.type = type;
        this.desc = desc;
        this.own = "";
        compress = false;
        compressSeparator = ';';
    }

    Column(Bean _parent, Column original) {
        super(_parent, original.name);
        desc = original.desc;
        type = original.type;
        own = original.own;
        if (original.foreignKey != null)
            foreignKey = new ForeignKey(this, original.foreignKey);
        if (original.keyRange != null)
            keyRange = new KeyRange(this, original.keyRange);

        compress = original.compress;
        compressSeparator = original.compressSeparator;
    }

    Column extract(Bean _parent, String _own) {
        if (own.contains(_own))
            return new Column(_parent, this);
        return null;
    }

    void resolveExtract() {
        if (foreignKey != null && foreignKey.invalid())
            foreignKey = null;
    }

    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "column");
        self.setAttribute("name", name);
        self.setAttribute("type", type);
        if (compress)
            self.setAttribute("compress", String.valueOf(compressSeparator));
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