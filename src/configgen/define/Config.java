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
        link = "[config]" + bean.name;
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
}
