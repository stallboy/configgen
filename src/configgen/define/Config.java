package configgen.define;

import configgen.Node;
import configgen.Utils;
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
        keys = self.getAttribute("keys").split(",");
    }

    public Config(ConfigCollection parent, String name) {
        super(parent, "");
        bean = new Bean(this, name);
        link = "[config]" + name;
        enumStr = "";
        keys = new String[0];
    }

    public void save(Element parent) {
        Element self = Utils.newChild(parent, "config");
        bean.update(self);
        if (!enumStr.isEmpty())
            self.setAttribute("enum", enumStr);
        if (keys.length > 0)
            self.setAttribute("keys", String.join(",", keys));
    }
}
