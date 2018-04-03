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
        for (Bean bean : beans.values()) {
            try {
                Bean pb = bean.extract(part, own);
                if (pb != null)
                    part.beans.put(bean.name, pb);
            }catch (Throwable e){
                throw new AssertionError(bean.name + ",从这个结构体抽取[" + own + "]出错", e);
            }
        }

        for (Table table : tables.values()) {
            try {
                Table pc = table.extract(part, own);
                if (pc != null)
                    part.tables.put(table.name, pc);
            }catch (Throwable e){
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
            }catch (Throwable e){
                throw new AssertionError(bean.name + ",解析这个结构体抽取部分出错", e);
            }
        }
        for (Table table : tables.values()) {
            try {
                table.resolveExtract();
            }catch (Throwable e){
                throw new AssertionError(table.name + ",解析这个表结构抽取部分出错", e);
            }
        }
    }

    public void save(File file, String encoding) throws IOException {
        Document doc = DomUtils.newDocument();
        save(doc);
        DomUtils.prettySaveDocument(doc, file, encoding);
    }

    private void save(Document doc) {
        Element self = doc.createElement("db");
        doc.appendChild(self);
        beans.values().forEach(b -> b.save(self));
        tables.values().forEach(t -> t.save(self));
    }

}
