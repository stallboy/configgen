package configgen.define;

import configgen.Node;
import configgen.util.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class Include extends Node {
    public final AllDefine define;
    public final String file;
    public final String pkgName;

    final Map<String, Bean> beans = new TreeMap<>();
    final Map<String, Table> tables = new TreeMap<>();

    Include(AllDefine parent, Element self) {
        this(parent, self.getAttribute("file"));
    }

    Include(AllDefine parent, String file) {
        super(parent, "include");

        this.define = parent;
        Path includeXmlPath = parent.resolvePath(file);
        this.file = define.formatXmlFilePath(includeXmlPath);
        this.pkgName = parent.childDataPathToPkgName(includeXmlPath.getParent());

        if (!Files.exists(includeXmlPath)) {
            return;
        }

        Element self = DomUtils.rootElement(includeXmlPath.toFile());
        DomUtils.permitAttributes(self); //datadir规定必须是本目录

        DomUtils.permitElements(self, "bean", "table");

        for (Element e : DomUtils.elements(self, "bean")) {
            Bean b = new Bean(define, e);
            require(null == beans.put(b.name, b), "Bean定义名字重复", b.name);
        }

        for (Element e : DomUtils.elements(self, "table")) {
            Table t = new Table(define, e);
            require(null == tables.put(t.name, t), "表定义名字重复", t.name);
            require(!beans.containsKey(t.name), "表和Bean定义名字重复", t.name);
        }
    }

    void clear() {
        beans.clear();
        tables.clear();
    }

    boolean isEmpty() {
        return beans.isEmpty() && tables.isEmpty();
    }

    void save(Element parent, String name) {
        Element self = DomUtils.newChild(parent, name);
        self.setAttribute("file", file);
    }

    void saveToXml(String encoding) {
        Path includeXmlPath = define.resolvePath(file);
        if (isEmpty()) {
            try {
                if (Files.deleteIfExists(includeXmlPath)) {
                    System.out.println("Delete File: " + includeXmlPath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        Document doc = DomUtils.newDocument();

        Element self = doc.createElement("db");
        doc.appendChild(self);
        //self.setAttribute("datadir", "."); 约定只能在datadir同目录，所以不要这个属性了

        for (Bean b : beans.values()) {
            b.save(self);
        }
        for (Table t : tables.values()) {
            t.save(self);
        }

        DomUtils.prettySaveDocument(doc, includeXmlPath.toFile(), encoding);
    }

}
