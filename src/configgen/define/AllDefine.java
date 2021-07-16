package configgen.define;

import configgen.Node;
import configgen.data.AllData;
import configgen.data.DTable;
import configgen.type.AllType;
import configgen.util.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class AllDefine extends Node {
    private final Path xmlPath;
    private final String encoding;

    private final DefineOption option;
    private Path dataDir;

    private final Map<String, Include> includes = new TreeMap<>();
    private final Map<String, Include> excludes = new TreeMap<>();

    private final Map<String, Bean> beans = new TreeMap<>();
    private final Map<String, Table> tables = new TreeMap<>();
    /**
     * import其他文件，其他文件也可再import。
     * 方便xml文件的组织，你可以全部csv就一个xml，也可以每个文件夹一个xml，最后总的一个xml来汇总。
     * 比如可以区分客户端用的xml，服务器用的xml。所以这里就存在抽取data的两个机制，一个xml，一个own，任君选择
     */
    private final Map<String, Import> imports = new TreeMap<>();


    /**
     * 要对上层隐藏import的机制，这里为效率cache下来。
     */
    private final Map<String, Bean> cachedAllBeans = new TreeMap<>();
    private final Map<String, Table> cachedAllTables = new TreeMap<>();

    /**
     * fullDefine才有，所谓full就是全部的定义，不能是从own抽取的
     */
    private AllData thisData;
    private final Map<String, DTable> cachedAllDataTables = new TreeMap<>();


    public AllDefine(Path _xmlPath, String _encoding) {
        super(null, "AllDefine");
        xmlPath = _xmlPath;
        encoding = _encoding;

        if (!Files.exists(xmlPath)) {
            option = new DefineOption();
            return;
        }

        Element self = DomUtils.rootElement(xmlPath.toFile());

        option = new DefineOption(self);
        dataDir = resolvePath(option.dataDir);

        DomUtils.permitElements(self, "import", "include", "exclude", "bean", "table");

        for (Element e : DomUtils.elements(self, "import")) {
            Import imp = new Import(this, e, encoding);
            require(null == imports.put(imp.file, imp), "import file重复", imp.file);
        }

        for (Element e : DomUtils.elements(self, "include")) {
            Include in = new Include(this, e);
            require(null == includes.put(in.file, in), "include file重复", in.file);
        }

        for (Element e : DomUtils.elements(self, "exclude")) {
            Include ex = new Include(this, e);
            require(null == excludes.put(ex.file, ex), "exclude file重复", ex.file);
        }

        // 自动扫描未指定的xml定义文件
        TreeSet<String> unspecifiedPkgXmlFiles = scanIncludeXmlFiles();
        unspecifiedPkgXmlFiles.removeAll(includes.keySet());
        unspecifiedPkgXmlFiles.removeAll(excludes.keySet());
        for (String file : unspecifiedPkgXmlFiles) {
            Include in = new Include(this, file);
            if (option.unspecifiedPkg == DefineOption.UnspecifiedPkg.Exclude) {
                excludes.put(in.file, in);
            } else {
                includes.put(in.file, in);
            }
        }

        Set<String> intersection = new TreeSet<>(includes.keySet());
        intersection.retainAll(excludes.keySet());
        require(intersection.isEmpty(), "include 和 exclude 存在交集. intersection = " + intersection);

        for (Element e : DomUtils.elements(self, "bean")) {
            Bean b = new Bean(this, e);
            require(null == beans.put(b.name, b), "Bean定义名字重复", b.name);
        }

        for (Element e : DomUtils.elements(self, "table")) {
            Table t = new Table(this, e);
            require(null == tables.put(t.name, t), "表定义名字重复", t.name);
            require(!beans.containsKey(t.name), "表和Bean定义名字重复", t.name);
        }

        for (Include in : includes.values()) {
            importBeanAndTables(in);
        }
        for (Include ex : excludes.values()) {
            importBeanAndTables(ex);
        }

        updateCache();
    }

    private void importBeanAndTables(Include include) {
        for (Bean b : include.beans.values()) {
            require(null == beans.put(b.name, b), "[include]Bean定义名字重复", b.name);
            require(!tables.containsKey(b.name), "[include]Bean定义名字和表重复", b.name);
        }
        for (Table t : include.tables.values()) {
            require(null == tables.put(t.name, t), "[include]表定义名字重复", t.name);
            require(!beans.containsKey(t.name), "[include]表和Bean定义名字重复", t.name);
        }
    }

    private void updateCache() {
        cachedAllBeans.clear();
        cachedAllBeans.putAll(beans);
        for (Import imp : imports.values()) {
            for (Bean b : imp.define.cachedAllBeans.values()) {
                require(null == cachedAllBeans.put(b.name, b), "[import]Bean定义名字重复", b.name);
                require(!cachedAllTables.containsKey(b.name), "[import]表和Bean定义名字重复", b.name);
            }
        }

        cachedAllTables.clear();
        cachedAllTables.putAll(tables);
        for (Import imp : imports.values()) {
            for (Table t : imp.define.cachedAllTables.values()) {
                require(null == cachedAllTables.put(t.name, t), "[import]表定义名重复", t.name);
                require(!cachedAllBeans.containsKey(t.name), "[import]表和Bean定义名字重复", t.name);
            }
        }
    }

    Path resolvePath(String file) {
        return xmlPath.resolveSibling(file).normalize();
    }

    Path resolveDataPath(String file) {
        return dataDir.resolve(file).normalize();
    }


    //////////////////////////////// 对上层接口，隐藏import导致的层级Data和层级Table


    public Path getDataDir() {
        return dataDir;
    }

    public String getEncoding() {
        return encoding;
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

    public DTable getDTable(String tableName) {
        return cachedAllDataTables.get(tableName);
    }


    //////////////////////////////// 读取数据文件，并补充完善Define

    /**
     *  读取数据文件，并补充完善Define，解析带类型的fullType
     */
    public AllType readData_AutoFix_ResolveType() {
        AllType firstTryType = DefineView.fullDefine(this).buildAllType();

        readDataFilesAndAutoFix(firstTryType);

        saveToXml();

        return resolveFullTypeAndAttachToData();
    }


    /**
     * 自动从Data中提取头两行的定义信息，填充Define
     */
    private void readDataFilesAndAutoFix(AllType firstTryType) {
        for (Import imp : imports.values()) {
            imp.define.readDataFilesAndAutoFix(firstTryType);
        }

        thisData = new AllData(this);
        thisData.autoFixDefine(this, firstTryType);


        updateCache();
        cachedAllDataTables.clear();
        cachedAllDataTables.putAll(thisData.getDTables());
        for (Import imp : imports.values()) {
            cachedAllDataTables.putAll(imp.define.cachedAllDataTables);
        }
    }

    public boolean excludeDataFile(Path dataFilePath) {
        String includeXmlFile = xmlPath.getParent().relativize(dataFilePath).toString();
        return excludes.containsKey(includeXmlFile);
    }

    public boolean removeTableIfNotExclude(String tableName) {
        String includeXmlFile = nameToIncludeXmlFile(tableName);
        if (!excludes.containsKey(includeXmlFile)) {
            tables.remove(tableName);
            return true;
        }
        return false;
    }

    /**
     * 保存回xml
     */
    public void saveToXml() {
        for (Import imp : imports.values()) {
            imp.define.saveToXml();
        }

        TreeMap<String, Bean> splitBeans = new TreeMap<>(this.beans);
        TreeMap<String, Table> splitTables = new TreeMap<>(this.tables);

        // 先clear，如果是PkgBasedSplit模式会再次填充数据
        for (Include include : includes.values()) {
            include.clear();
        }
        for (Include exclude : excludes.values()) {
            exclude.clear();
        }
        if (option.splitMode == DefineOption.SplitMode.PkgBased) {
            pkgBasedSplit(splitBeans, splitTables);
        }

        for (Include include : new ArrayList<>(includes.values())) {
            include.saveToXml(encoding);

            if (include.isEmpty()) {
                includes.remove(include.file);
            }
        }
        for (Include exclude : new ArrayList<>(excludes.values())) {
            exclude.saveToXml(encoding);

            if (exclude.isEmpty()) {
                excludes.remove(exclude.file);
            }
        }

        save(splitBeans, splitTables);
    }

    /**
     * 解析出类型，把齐全的类型信息 赋到 Data上，因为之后生成Value时可能只会用 不全的Type
     */
    private AllType resolveFullTypeAndAttachToData() {
        AllType fullType = DefineView.fullDefine(this).buildAllType();
        attachTypeToData(fullType);
        return fullType;
    }

    void attachTypeToData(AllType type) {
        thisData.attachType(type);
        for (Import imp : imports.values()) {
            imp.define.attachTypeToData(type);
        }
    }


    //////////////////////////////// auto fix使用的接口

    public Set<String> getTableNames() {
        return new HashSet<>(tables.keySet());
    }

    public Table newTable(String tableName) {
        Table t = new Table(this, tableName);
        tables.put(tableName, t);
        return t;
    }

    //////////////////////////////// extract

    /**
     * 根据own和exclude抽取出定义视图
     * @param own 配置为own="client,xeditor"的column就会被resolvePartType("client")抽取出来
     * @return 一个抽取过后的带类型结构信息。
     * 用于对上层隐藏掉own机制 和 exclude机制。
     */
    // 返回的是全新的 部分的Type
    public AllType resolvePartType(String own) {
        return new DefineView(this, own).buildAllType();
    }

    void extract(DefineView defineView, String own) {
        for (Import imp : imports.values()) {
            imp.extract(defineView, own);
        }

        for (Bean bean : beans.values()) {
            try {
                Bean pb = bean.extract(defineView, defineView);
                if (pb != null)
                    defineView.beans.put(bean.name, pb);
            } catch (Throwable e) {
                throw new AssertionError(bean.name + ",从这个结构体抽取[" + own + "]出错", e);
            }
        }

        for (Table table : tables.values()) {
            try {
                if (!defineView.isIgnoreExcludes()) {
                    String includeXmlFile = nameToIncludeXmlFile(table.name);
                    if (excludes.containsKey(includeXmlFile)) {
                        // 表所在的目录都被排除
                        continue;
                    }
                }

                Table pc = table.extract(defineView);
                if (pc != null)
                    defineView.tables.put(table.name, pc);
            } catch (Throwable e) {
                throw new AssertionError(table.name + ",从这个表结构抽取[" + own + "]出错", e);
            }
        }
    }

    //////////////////////////////// save

    private void save(TreeMap<String, Bean> splitBeans, TreeMap<String, Table> splitTables) {
        Document doc = DomUtils.newDocument();
        doc.appendChild(doc.createComment(DefineOption.commentText));

        Element self = doc.createElement("db");
        doc.appendChild(self);
        option.save(self);

        for (Import imp : imports.values()) {
            imp.save(self);
        }

        if (!option.simplify || option.unspecifiedPkg == DefineOption.UnspecifiedPkg.Exclude) {
            for (Include include : includes.values()) {
                include.save(self, "include");
            }
        }
        if (!option.simplify || option.unspecifiedPkg == DefineOption.UnspecifiedPkg.Include) {
            for (Include exclude : excludes.values()) {
                exclude.save(self, "exclude");
            }
        }

        for (Bean b : splitBeans.values()) {
            b.save(self);
        }
        for (Table t : splitTables.values()) {
            t.save(self);
        }

        DomUtils.prettySaveDocument(doc, xmlPath.toFile(), encoding);
    }

    ///////////////////////////////////////////// split(include or exclude) xml

    private TreeSet<String> scanIncludeXmlFiles() {
        TreeSet<String> includeXmlFiles = new TreeSet<>();
        try {
            Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes a) {
                    File file = path.toFile();
                    // 必须是xml
                    if (!file.getName().endsWith(".xml")) {
                        return FileVisitResult.CONTINUE;
                    }

                    // 文件名必须和目录名一样
                    String fileName = file.getName();
                    fileName = fileName.substring(0, fileName.length() - ".xml".length());
                    if (!fileName.equals(file.getParentFile().getName())) {
                        return FileVisitResult.CONTINUE;
                    }

                    // 不能是xmlPath文件自己
                    if (dataDir.equals(xmlPath)) {
                        return FileVisitResult.CONTINUE;
                    }

                    // 在顶级目录下，忽略
                    if (path.getParent().equals(dataDir)) {
                        return FileVisitResult.CONTINUE;
                    }

                    String includeXmlFile = formatXmlFilePath(path);
                    includeXmlFiles.add(includeXmlFile);

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return includeXmlFiles;
    }

    private void pkgBasedSplit(TreeMap<String, Bean> splitBeans, TreeMap<String, Table> splitTables) {
        for (Table t : this.tables.values()) {
            String file = nameToIncludeXmlFile(t.name);
            if (file.isEmpty()) {
                continue;
            }
            Include include = excludes.get(file); // 先重excludes中找，找到的话表示被排除了
            if (include == null) {
                include = includes.computeIfAbsent(file, f -> new Include(this, f));
            }
            include.tables.put(t.name, t);
            splitTables.remove(t.name);
        }

        for (Bean b : this.beans.values()) {
            String file = nameToIncludeXmlFile(b.name);
            if (file.isEmpty()) {
                continue;
            }
            Include include = excludes.get(file); // 先重excludes中找，找到的话表示被排除了
            if (include == null) {
                include = includes.computeIfAbsent(file, f -> new Include(this, f));
            }
            include.beans.put(b.name, b);
            splitBeans.remove(b.name);
        }
    }

    private String nameToIncludeXmlFile(String name) {
        int i = name.lastIndexOf('.');
        if (i <= 0) {
            //定义在顶级目录，不用再分割到子目录中
            return "";
        }
        String innerPkgName = name.substring(0, i);
        String relativePath = innerPkgName.replace(".", "/");
        Path includeXmlPath = resolveDataPath(relativePath).normalize();
        includeXmlPath = includeXmlPath.resolve(includeXmlPath.toFile().getName() + ".xml");

        return formatXmlFilePath(includeXmlPath);
    }

    String formatXmlFilePath(Path includeXmlPath) {
        return xmlPath.getParent().relativize(includeXmlPath).normalize().toString().replace("\\", "/");
    }

    // 约定includeXmlFile必须和它代表的配置放在同一目录
    String childDataPathToPkgName(Path childDataDir) {
        String relativePath = dataDir.relativize(childDataDir).normalize().toString();
        return relativePath.replace("\\", "/").replace("/", ".");
    }

}
