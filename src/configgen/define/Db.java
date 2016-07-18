package configgen.define;

import configgen.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class Db extends Node {
    public final Map<String, Bean> beans = new TreeMap<>();
    public final Map<String, Table> tables = new TreeMap<>();

    public Db(File file) {
        this(file.exists() ? DomUtils.rootElement(file) : null);
    }

    private Db(Element self) {
        super(null, "db");
        if (self != null) {
            DomUtils.permitAttributes(self);
            DomUtils.permitElements(self, "bean", "table");

            for (Element e : DomUtils.elements(self, "bean")) {
                Bean b = new Bean(this, e);
                require(null == beans.put(b.name, b), "bean duplicate name=" + b.name);
            }

            for (Element e : DomUtils.elements(self, "table")) {
                Table t = new Table(this, e);
                require(null == tables.put(t.bean.name, t), "table duplicate name=" + t.bean.name);
                require(!beans.containsKey(t.bean.name), "table bean duplicate name=" + t.bean.name);
            }
        }
    }

    private Db(String own) {
        super(null, "db(" + own + ")");
    }

    public void checkInclude(Db stable) {
        stable.beans.forEach((sname, sbean) -> {
            Bean bean = beans.get(sname);
            require(bean != null, sname + " in stable not in develop version");
            bean.checkInclude(sbean);
        });
        stable.tables.forEach((sname, stab) -> {
            Table table = tables.get(sname);
            require(table != null, sname + " in stable not in develop version");
            table.checkInclude(stab);
        });
    }

    public Table newTable(String tableName) {
        Table t = new Table(this, tableName);
        tables.put(tableName, t);
        return t;
    }

    public Column newColumn(Table table, String colName, String colType, String colDesc) {
        Column c = new Column(table.bean, colName, colType, colDesc);
        table.bean.columns.put(colName, c);
        return c;
    }

    public Db extract(String own) {
        Db part = new Db(own);
        beans.forEach((k, v) -> {
            Bean pb = v.extract(part, own);
            if (pb != null)
                part.beans.put(k, pb);
        });

        tables.forEach((k, v) -> {
            Table pc = v.extract(part, own);
            if (pc != null)
                part.tables.put(k, pc);
        });

        part.resolveExtract();
        return part;
    }

    void resolveExtract() {
        beans.values().forEach(Bean::resolveExtract);
        tables.values().forEach(Table::resolveExtract);
    }

    public void save(File file, String encoding) throws IOException {
        Document doc = DomUtils.newDocument();
        save(doc);
        DomUtils.prettySaveDocument(doc, file, encoding);
    }

    void save(Document doc) {
        Element self = doc.createElement("db");
        doc.appendChild(self);
        beans.values().forEach(b -> b.save(self));
        tables.values().forEach(t -> t.save(self));
    }

}
