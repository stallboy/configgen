package configgen.define;

import configgen.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConfigCollection extends Node {
    public final Map<String, Bean> beans = new TreeMap<>();
    public final Map<String, Config> configs = new TreeMap<>();

    public ConfigCollection(File file) {
        this(file.exists() ? DomUtils.rootElement(file) : null);
    }

    private ConfigCollection(Element self) {
        super(null, "define");
        if (self != null) {
            DomUtils.attributes(self);
            List<List<Element>> elements = DomUtils.elementsList(self, "bean", "config");

            for (Element e : elements.get(0)) {
                Bean b = new Bean(this, e);
                require(null == beans.put(b.name, b), "bean duplicate name=" + b.name);
            }

            for (Element e : elements.get(1)) {
                Config c = new Config(this, e);
                require(null == configs.put(c.bean.name, c), "config duplicate name=" + c.bean.name);
                require(!beans.containsKey(c.bean.name), "config bean duplicate name=" + c.bean.name);
            }
        }
    }

    private ConfigCollection(String own) {
        super(null, "define(" + own + ")");
    }

    public void save(File file, String encoding) throws IOException {
        Document doc = DomUtils.newDocument();
        save(doc);
        DomUtils.prettySaveDocument(doc, file, encoding);
    }

    private void save(Document doc) {
        Element self = doc.createElement("configcollection");
        doc.appendChild(self);
        beans.values().forEach(b -> b.save(self));
        configs.values().forEach(c -> c.save(self));
    }

    public ConfigCollection extract(String own) {
        ConfigCollection part = new ConfigCollection(own);
        beans.forEach((k, v) -> {
            Bean pb = v.extract(part, own);
            if (pb != null)
                part.beans.put(k, pb);
        });

        configs.forEach((k, v) -> {
            Config pc = v.extract(part, own);
            if (pc != null)
                part.configs.put(k, pc);
        });

        part.resolveExtract();
        return part;
    }

    private void resolveExtract() {
        beans.values().forEach(Bean::resolveExtract);
        configs.values().forEach(Config::resolveExtract);
    }

}
