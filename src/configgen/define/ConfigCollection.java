package configgen.define;

import configgen.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConfigCollection extends Node {
    public final Map<String, Bean> beans = new TreeMap<>();
    public final Map<String, Config> configs = new TreeMap<>();

    public ConfigCollection(File file) throws IOException, SAXException, ParserConfigurationException {
        this(file.exists() ? DomUtils.rootElement(file) : null);
    }

    private ConfigCollection(Element self) {
        super(null, "define");
        if (self != null) {
            DomUtils.attributes(self);
            List<List<Element>> elements = DomUtils.elementsList(self, "bean", "config");

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
    }

    private ConfigCollection(String own) {
        super(null, "define(" + own + ")");
    }

    public void save(File file, String encoding) throws IOException, ParserConfigurationException {
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
            Bean o = v.extract(this, null, own);
            if (o != null)
                part.beans.put(k, o);
        });

        configs.forEach((k, v) -> {
            Config o = v.extract(this, own);
            if (o != null)
                part.configs.put(k, o);
        });

        part.extract2();
        return part;
    }

    private void extract2() {
        beans.values().forEach(Bean::extract2);
        configs.values().forEach(Config::extract2);
    }

}
