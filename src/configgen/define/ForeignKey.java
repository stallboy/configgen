package configgen.define;

import configgen.Node;
import configgen.util.DomUtils;
import org.w3c.dom.Element;

public class ForeignKey extends Node {
    public enum RefType {
        NORMAL,
        NULLABLE, // 单元格可以什么都不填
        LIST      // 可以外键到其他表的非unique key
    }

    public final String[] keys;
    public Ref ref;
    public RefType refType;
    public Ref mapKeyRef;

    ForeignKey(Bean _parent, Element self) {
        super(_parent, self.getAttribute("name"));
        DomUtils.permitAttributes(self, "name", "keys", "ref", "refType", "keyRef");
        keys = DomUtils.parseStringArray(self, "keys");
        init(self);
    }

    ForeignKey(Column _parent, Element self) {
        super(_parent, self.getAttribute("name"));
        keys = new String[]{name};
        init(self);
    }

    private void init(Element self) {
        ref = new Ref(self.getAttribute("ref"));
        if (self.hasAttribute("refType")) {
            refType = RefType.valueOf(self.getAttribute("refType").toUpperCase());
        } else {
            refType = RefType.NORMAL;
        }
        String keyref = self.getAttribute("keyRef");
        if (keyref.isEmpty()) {
            mapKeyRef = null;
        } else {
            mapKeyRef = new Ref(keyref);
        }
    }

    ForeignKey(Node _parent, ForeignKey original) {
        super(_parent, original.name);
        keys = original.keys;
        ref = original.ref;
        refType = original.refType;
        mapKeyRef = original.mapKeyRef;
    }

    boolean invalid() {
        Db db = (Db) root;
        return !(ref.valid(db) && (mapKeyRef == null || mapKeyRef.valid(db)));
    }

    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "foreignKey");
        self.setAttribute("name", name);
        self.setAttribute("keys", String.join(",", keys));
        update(self);
    }

    void update(Element self) {
        self.setAttribute("ref", ref.toString());
        if (refType != RefType.NORMAL)
            self.setAttribute("refType", refType.toString());
        if (mapKeyRef != null)
            self.setAttribute("keyRef", mapKeyRef.toString());
    }
}
