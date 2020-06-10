package configgen.define;

import configgen.Node;
import configgen.data.AllData;
import configgen.data.DTable;
import configgen.type.AllType;
import configgen.util.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class AllDefine extends Node {
    private final Map<String, Bean> beans = new TreeMap<>();
    private final Map<String, Table> tables = new TreeMap<>();
    private final Map<String, Import> imports = new TreeMap<>();

    private Path xmlPath;
    private String encoding;

    private String dataDirStr;
    private Path dataDir;

    private final Map<String, Bean> cachedAllBeans = new TreeMap<>();
    private final Map<String, Table> cachedAllTables = new TreeMap<>();


    public AllDefine(Path _xmlPath, String _encoding) {
        super(null, "AllDefine");
        xmlPath = _xmlPath;
        encoding = _encoding;
        dataDirStr = ".";

        if (!Files.exists(xmlPath)) {
            return;
        }

        Element self = DomUtils.rootElement(xmlPath.toFile());
        DomUtils.permitAttributes(self, "datadir");

        if (self.hasAttribute("datadir")) {
            dataDirStr = self.getAttribute("datadir");
            dataDir = resolvePath(dataDirStr);
        } else {
            dataDir = xmlPath.getParent(); //默认当前xml文件所在目录
        }
        DomUtils.permitElements(self, "import", "bean", "table");

        for (Element e : DomUtils.elements(self, "import")) {
            Import imp = new Import(this, e, encoding);
            require(null == imports.put(imp.file, imp), "import file重复", imp.file);
        }

        for (Element e : DomUtils.elements(self, "bean")) {
            Bean b = new Bean(this, e);
            require(null == beans.put(b.name, b), "Bean定义名字重复", b.name);
        }

        for (Element e : DomUtils.elements(self, "table")) {
            Table t = new Table(this, e);
            require(null == tables.put(t.name, t), "表定义名字重复", t.name);
            require(!beans.containsKey(t.name), "表和Bean定义名字重复", t.name);
        }

        fixCache();
    }

    private void fixCache() {
        cachedAllBeans.putAll(beans);
        for (Import imp : imports.values()) {
            cachedAllBeans.putAll(imp.define.cachedAllBeans);
        }

        cachedAllTables.putAll(tables);
        for (Import imp : imports.values()) {
            cachedAllTables.putAll(imp.define.cachedAllTables);
        }
    }

    Path resolvePath(String file) {
        return xmlPath.getParent().resolve(file);
    }

    //////////////////////////////// 对上层接口，隐藏import导致的层级Data和层级Table

    private AllData thisData;
    private AllType fullType; //只有顶层Define有
    private final Map<String, DTable> cachedAllDataTables = new TreeMap<>();


    public Path getDataDir() {
        return dataDir;
    }

    public Collection<Bean> getAllBeans() {
        return cachedAllBeans.values();
    }

    public Collection<Table> getAllTables() {
        return cachedAllTables.values();
    }

    public Table getTable(String tableName) {
        return cachedAllTables.get(tableName);
    }


    public AllType getFullType() {
        return fullType;
    }

    public DTable getDTable(String tableName) {
        return cachedAllDataTables.get(tableName);
    }


    //////////////////////////////// 读取数据文件，并补充完善Define

    public void readDataFilesThenAutoFix() {
        AllType firstTryType = new AllType(this);
        firstTryType.resolve();

        autoFixByData(firstTryType);
        save();

        fullType = new AllType(this);
        fullType.resolve();
        attachTypeToData(fullType);
    }

    // 自动从Data种提取头两行的定义信息，填充Define，保存到xml
    void autoFixByData(AllType firstTryType) {
        for (Import imp : imports.values()) {
            imp.define.autoFixByData(firstTryType);
        }
        thisData = new AllData(dataDir, encoding);
        thisData.autoFixDefine(this, firstTryType);

        cachedAllDataTables.putAll(thisData.getDTables());
        for (Import imp : imports.values()) {
            cachedAllDataTables.putAll(imp.define.cachedAllDataTables);
        }
    }

    // 把齐全的类型信息 赋到 Data上，因为之后生成Value时可能只会用 不全的Type
    void attachTypeToData(AllType type) {
        thisData.attachType(type);
        for (Import imp : imports.values()) {
            imp.define.attachTypeToData(type);
        }
    }


    public void clearTables() {
        tables.clear();
    }

    public void addTable(Table table) {
        tables.put(table.name, table);
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


    //////////////////////////////// extract

    private AllDefine(String own) {
        super(null, "AllDefine(" + own + ")");
    }


    // 返回的是全新的 部分的Define

    public AllDefine extractOwn(String own) {
        AllDefine topPart = extract(own);
        topPart.resolveExtract(topPart);
        return topPart;
    }


    AllDefine extract(String own) {
        AllDefine part = new AllDefine(own);

        for (Import imp : imports.values()) {
            Import pi = imp.extract(part, own);
            part.imports.put(pi.file, pi);
        }

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

        part.fixCache();
        return part;
    }

    void resolveExtract(AllDefine top) {
        for (Import imp : imports.values()) {
            imp.resolveExtract(top);
        }

        for (Bean bean : beans.values()) {
            try {
                bean.resolveExtract(top);
            } catch (Throwable e) {
                throw new AssertionError(bean.name + ",解析这个结构体抽取部分出错", e);
            }
        }
        for (Table table : tables.values()) {
            try {
                table.resolveExtract(top);
            } catch (Throwable e) {
                throw new AssertionError(table.name + ",解析这个表结构抽取部分出错", e);
            }
        }
    }


    //////////////////////////////// save

    public void save() {
        Document doc = DomUtils.newDocument();

        Element self = doc.createElement("db");
        doc.appendChild(self);
        self.setAttribute("datadir", dataDirStr);

        for (Import imp : imports.values()) {
            imp.save(self);
        }

        for (Bean b : beans.values()) {
            b.save(self);
        }
        for (Table t : tables.values()) {
            t.save(self);
        }

        DomUtils.prettySaveDocument(doc, xmlPath.toFile(), encoding);
    }


}
