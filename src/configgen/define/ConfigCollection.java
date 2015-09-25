package configgen.define;

import configgen.Node;
import configgen.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConfigCollection extends Node {
    public final Map<String, Bean> beans = new TreeMap<>();
    public final Map<String, Config> configs = new TreeMap<>();

    public ConfigCollection(Element self) {
        super(null, "define");
        Utils.attributes(self);
        List<List<Element>> elements = Utils.elementsList(self, "bean", "config");

        for (Element e : elements.get(0)) {
            Bean b = new Bean(this, null, e);
            Assert(null == beans.put(b.name, b), "bean duplicate name=" + b.name);
        }

        for (Element e : elements.get(1)) {
            Config c = new Config(this, e);
            Assert(null == configs.put(c.bean.name, c), "config duplicate name=" + c.bean.name);
            Assert(!beans.containsKey(c.bean.name), "config bean duplicate name=" + c.bean.name);
        }
    }

    public void save(Document doc){
        Element self = doc.createElement("configcollection");
        doc.appendChild(self);
        beans.values().forEach(b -> b.save(self));
        configs.values().forEach(c -> c.save(self));
    }
}
