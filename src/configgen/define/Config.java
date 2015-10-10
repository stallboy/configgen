package configgen.define;

import configgen.Node;
import org.w3c.dom.Element;

public class Config extends Node {
    public final Bean bean;
    public final String enumStr;
    public final String[] keys;

    public Config(ConfigCollection parent, Element self) {
        super(parent, "");
        bean = new Bean(parent, this, self);
        link = bean.name;
        enumStr = self.getAttribute("enum");
        String k = self.getAttribute("keys").trim();
        if (!k.isEmpty())
            keys = k.split(",");
        else
            keys = new String[0];
    }

    public Config(ConfigCollection parent, String name) {
        super(parent, name);
        bean = new Bean(this, name);
        enumStr = "";
        keys = new String[0];
    }

    private Config(ConfigCollection parent, Config original, Bean ownBean) {
        super(parent, original.link);
        bean = ownBean;
        enumStr = bean.fields.containsKey(original.enumStr) ? original.enumStr : "";
        keys = original.keys;
        if (keys.length > 0) {
            for (String key : keys) {
                Assert(bean.fields.containsKey(key), "must own primary keys");
            }
        } else {
            Assert(bean.fields.containsKey(original.bean.fields.keySet().iterator().next()), "must own primary key");
        }

    }

    public void save(Element parent) {
        Element self = DomUtils.newChild(parent, "config");
        bean.update(self);
        if (!enumStr.isEmpty())
            self.setAttribute("enum", enumStr);
        if (keys.length > 0)
            self.setAttribute("keys", String.join(",", keys));
    }

    public Config extract(ConfigCollection parent, String own) {
        Bean ownBean = bean.extract(parent, this, own);
        if (ownBean == null)
            return null;
        return new Config(parent, this, ownBean);
    }

    void extract2() {
        bean.extract2();
    }
}
