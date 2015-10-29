package configgen.define;

import configgen.Node;
import org.w3c.dom.Element;

public class Config extends Node {
    public Bean bean;
    public String enumStr;
    public final String[] keys;

    public Config(ConfigCollection parent, Element self) {
        super(parent, self.getAttribute("name"));
        bean = new Bean(this, self);
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

    private Config(ConfigCollection _parent, Config original) {
        super(_parent, original.name);
        enumStr = original.enumStr;
        keys = original.keys.clone();
    }

    Config extract(ConfigCollection _parent, String own) {
        Config part = new Config(_parent, this);
        Bean pb = bean.extract(part, own);
        if (pb == null)
            return null;
        part.bean = pb;
        return part;
    }

    void resolveExtract() {
        bean.resolveExtract();
        String original = enumStr;
        enumStr = bean.fields.containsKey(original) ? original : "";

        if (keys.length > 0) {
            for (String key : keys) {
                require(bean.fields.containsKey(key), "must own primary keys");
            }
        }
    }

    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "config");
        bean.update(self);
        if (!enumStr.isEmpty())
            self.setAttribute("enum", enumStr);
        if (keys.length > 0)
            self.setAttribute("keys", String.join(",", keys));
    }
}
