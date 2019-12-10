package configgen.define;

import configgen.Node;
import configgen.util.DomUtils;
import org.w3c.dom.Element;

public class ForeignKey extends Node {
    public enum RefType {
        NORMAL,   // 链接到表的主键或唯一键，不能为空
        NULLABLE, // 格子里可以什么都不填
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
        String refstr = self.getAttribute("ref");
        if (!refstr.isEmpty()) {
            ref = new Ref(refstr);
            if (self.hasAttribute("refType")) {
                refType = RefType.valueOf(self.getAttribute("refType").toUpperCase());
            } else {
                refType = RefType.NORMAL;
            }
        } else {
            ref = null;
        }
        String keyref = self.getAttribute("keyRef"); // lua生成会直接忽略这种情况
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
        AllDefine allDefine = (AllDefine) root;
        return !((ref == null || ref.valid(allDefine)) && (mapKeyRef == null || mapKeyRef.valid(allDefine)));
    }

    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "foreignKey");
        self.setAttribute("name", name);
        self.setAttribute("keys", String.join(",", keys));
        update(self);
    }

    void update(Element self) {
        if (ref != null) {
            self.setAttribute("ref", ref.toString());
            if (refType != RefType.NORMAL)
                self.setAttribute("refType", refType.toString());
        }
        if (mapKeyRef != null) {
            self.setAttribute("keyRef", mapKeyRef.toString());
        }
    }
}
