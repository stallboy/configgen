package configgen.define;

import configgen.Node;
import configgen.util.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class AllDefine extends Node {
    private final Map<String, Bean> beans = new TreeMap<>();
    public final Map<String, Table> tables = new TreeMap<>();

    public Collection<Bean> getBeans(){
        return beans.values();
    }

    public AllDefine(File file) {
        this(file.exists() ? DomUtils.rootElement(file) : null);
    }

    private AllDefine(Element self) {
        super(null, "AllDefine");
        if (self != null) {
            DomUtils.permitAttributes(self);
            DomUtils.permitElements(self, "bean", "table");

            for (Element e : DomUtils.elements(self, "bean")) {
                Bean b = new Bean(this, e);
                require(null == beans.put(b.name, b), "Bean定义名字重复", b.name);
            }

            for (Element e : DomUtils.elements(self, "table")) {
                Table t = new Table(this, e);
                require(null == tables.put(t.bean.name, t), "表定义名字重复", t.bean.name);
                require(!beans.containsKey(t.bean.name), "表和Bean定义名字重复", t.bean.name);
            }
        }
    }

    private AllDefine(String own) {
        super(null, "AllDefine(" + own + ")");
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

    public AllDefine extract(String own) {
        AllDefine part = new AllDefine(own);
        for (Bean bean : beans.values()) {
            try {
                Bean pb = bean.extract(part, own);
                if (pb != null)
                    part.beans.put(bean.name, pb);
            } catch (Throwable e) {
                throw new AssertionError(bean.name + ",从这个结构体抽取[" + own + "]出错", e);
            }
        }

        for (Table table : tables.values()) {
            try {
                Table pc = table.extract(part, own);
                if (pc != null)
                    part.tables.put(table.name, pc);
            } catch (Throwable e) {
                throw new AssertionError(table.name + ",从这个表结构抽取[" + own + "]出错", e);
            }
        }


        part.resolveExtract();
        return part;
    }

    private void resolveExtract() {
        for (Bean bean : beans.values()) {
            try {
                bean.resolveExtract();
            } catch (Throwable e) {
                throw new AssertionError(bean.name + ",解析这个结构体抽取部分出错", e);
            }
        }
        for (Table table : tables.values()) {
            try {
                table.resolveExtract();
            } catch (Throwable e) {
                throw new AssertionError(table.name + ",解析这个表结构抽取部分出错", e);
            }
        }
    }

    public void save(File file, String encoding) {
        Document doc = DomUtils.newDocument();
        save(doc);
        DomUtils.prettySaveDocument(doc, file, encoding);
    }

    private void save(Document doc) {
        Element self = doc.createElement("db");
        doc.appendChild(self);
        for (Bean b : beans.values()) {
            b.save(self);
        }
        for (Table t : tables.values()) {
            t.save(self);
        }
    }

}
