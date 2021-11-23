package configgen.define;

import configgen.Node;
import configgen.data.AllData;
import configgen.type.AllType;
import configgen.util.FileNameExtract;
import configgen.view.DefineView;
import configgen.view.ViewFilter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class AllDefine extends Node {
    private final Path dataDir;
    private final String encoding;
    private final String topXmlFile = "config.xml";

    private final Map<String, Define> defines = new TreeMap<>();
    private final Map<String, Bean> beans = new TreeMap<>();
    private final Map<String, Table> tables = new TreeMap<>();

    public AllDefine(Path dataDir, String encoding) {
        super(null, "AllDefine");
        this.dataDir = dataDir.toAbsolutePath().normalize();
        this.encoding = encoding;

        if (Files.exists(dataDir)) {
            readFromXml();
        }
    }

    private Define.SplitMode getSplitMode() {
        Define topDefine = defines.get(topXmlFile);
        // 如果不存在topXmlFile，当PkgBased处理，否则按topXmlFile定义的splitMode处理
        return topDefine == null ? Define.SplitMode.PkgBased : topDefine.splitMode;
    }

    private void readFromXml() {
        // allInOne模式仍然扫描所有的define xml文件，但保存的时候会将所有table和bean定义放到一个文件中
        TreeSet<String> defineXmlFiles = scanDefineXmlFiles();
        for (String file : defineXmlFiles) {
            Define define = new Define(this, file);
            defines.put(define.file, define);

            for (Bean b : define.beans.values()) {
                require(null == beans.put(b.name, b), "[include]Bean定义名字重复", b.name);
                require(!tables.containsKey(b.name), "[include]Bean定义名字和表重复", b.name);
            }
            for (Table t : define.tables.values()) {
                require(null == tables.put(t.name, t), "[include]表定义名字重复", t.name);
                require(!beans.containsKey(t.name), "[include]表和Bean定义名字重复", t.name);
            }
        }
    }

    boolean isTopDefine(Define define) {
        return define.file.equals(topXmlFile);
    }

    Define getTopDefine() {
        return defines.computeIfAbsent(topXmlFile, f -> new Define(this, f));
    }

    /**
     * 保存回xml
     */
    public void saveToXml() {
        for (Define define : defines.values()) {
            define.tables.clear();
            define.beans.clear();
        }

        Define topDefine = getTopDefine();
        topDefine.beans.putAll(beans);
        topDefine.tables.putAll(tables);

        if (getSplitMode() == Define.SplitMode.PkgBased) {
            pkgBasedSplit(topDefine);
        }
        // 调整bean和tabe所属的define
        for (Define define : defines.values()) {
            define.rectify();
        }

        for (Define define : new ArrayList<>(defines.values())) {
            define.saveToXml(encoding);

            if (define.canDelete()) {
                defines.remove(define.file);
            }
        }
    }

    Path resolvePath(String file) {
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
        return beans.values();
    }

    public Collection<Table> getAllTables() {
        return tables.values();
    }

    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    public Bean getBean(String beanName) {
        return beans.get(beanName);
    }

    //////////////////////////////// 读取数据文件，并补充完善Define

    public AllData readData() {
        return new AllData(this);
    }

    public AllType autoFixDefineAndResolveFullType(AllData allData) {
        allData.autoFixDefine(this);

        saveToXml();

        // 解析出类型，把齐全的类型信息 赋到 Data上，因为之后生成Value时可能只会用 不全的Type
        AllType fullType = resolveType(ViewFilter.FULL_DEFINE);
        allData.attachType(fullType);

        return fullType;
    }

    public void removeTable(String tableName) {
        tables.remove(tableName);
    }

    //////////////////////////////// auto fix使用的接口

    public Set<String> getTableNames() {
        return new HashSet<>(tables.keySet());
    }

    public Table newTable(String tableName) {
        Define topDefine = getTopDefine();
        Table t = new Table(topDefine, tableName);
        Table old = tables.put(tableName, t);
        require(null == old, "newTable error. tableName=" + tableName);
        return t;
    }

    //////////////////////////////// extract

    /**
     * 根据own和exclude抽取出定义视图
     *
     * @param viewFilter 配置为own="client,xeditor"的column就会被resolvePartType("client")抽取出来，也可以是一个view_xxx.xml
     * @return 一个抽取过后的带类型结构信息。
     * 用于对上层隐藏掉own机制 和 exclude机制。
     */
    // 返回的是全新的 部分的Type
    public AllType resolveType(ViewFilter viewFilter) {
        return new DefineView(this, viewFilter).buildAllType();
    }

    public void extract(DefineView defineView) {
        for (Bean bean : beans.values()) {
            if (!defineView.filter.acceptBean(bean)) {
                continue;
            }
            try {
                Bean pb = bean.extract(defineView, defineView);
                defineView.beans.put(bean.name, Objects.requireNonNull(pb));
            } catch (Throwable e) {
                throw new AssertionError(bean.name + ",从这个结构体抽取[" + defineView.filter.name() + "]出错", e);
            }
        }

        for (Table table : tables.values()) {
            if (!defineView.filter.acceptTable(table)) {
                continue;
            }
            try {
                Table pc = table.extract(defineView);
                defineView.tables.put(table.name, Objects.requireNonNull(pc));
            } catch (Throwable e) {
                throw new AssertionError(table.name + ",从这个表结构抽取[" + defineView.filter.name() + "]出错", e);
            }
        }
    }


    ///////////////////////////////////////////// split define xml

    private TreeSet<String> scanDefineXmlFiles() {
        TreeSet<String> defineXmlFiles = new TreeSet<>();
        try {
            Files.walkFileTree(dataDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes a) {
                    if (isDefineXmlFile(path)) {
                        String defineXmlFile = formatDefineXmlFilePath(path);
                        defineXmlFiles.add(defineXmlFile);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return defineXmlFiles;
    }

    private boolean isDefineXmlFile(Path path) {
        String fileName = path.toFile().getName();
        // 必须是xml
        if (!fileName.endsWith(".xml")) {
            return false;
        }
        Path parentPath = path.getParent();
        // 顶级目录，有可能是config.xml
        if (dataDir.equals(parentPath)) {
            return fileName.equals(topXmlFile);
        } else {
            // 文件名必须和目录名一样
            String moduleName = fileName.substring(0, fileName.length() - 4);
            String parentDirName = parentPath.toFile().getName();

            return FileNameExtract.isFileNameExtractMatch(parentDirName, moduleName);
        }
    }

    private void pkgBasedSplit(Define topDefine) {
        for (Table t : this.tables.values()) {
            String file = nameToDefineXmlFile(t.name);
            if (file.isEmpty()) {
                continue;
            }
            Define currDefine = defines.computeIfAbsent(file, f -> new Define(this, f));
            currDefine.tables.put(t.name, t);
            topDefine.tables.remove(t.name);
        }

        for (Bean b : this.beans.values()) {
            String file = nameToDefineXmlFile(b.name);
            if (file.isEmpty()) {
                continue;
            }
            Define currDefine = defines.computeIfAbsent(file, f -> new Define(this, f));
            currDefine.beans.put(b.name, b);
            topDefine.beans.remove(b.name);
        }
    }

    String nameToDefineXmlFile(String name) {
        int i = name.lastIndexOf('.');
        if (i <= 0) {
            //定义在顶级目录，不用再分割到子目录中
            return "";
        }

        String pkg = name.substring(0, i);
        Path defineXmlPath = FileNameExtract.packageNameToPathName(dataDir, pkg);
        String xml = FileNameExtract.extractFileName(defineXmlPath.toFile().getName()) + ".xml";
        return formatDefineXmlFilePath(defineXmlPath.resolve(xml));
    }

    String formatDefineXmlFilePath(Path defineXmlPath) {
        return dataDir.relativize(defineXmlPath).normalize().toString().replace("\\", "/");
    }

    // 约定defineXmlFile必须和它代表的配置放在同一目录
    public String childDataPathToPkgName(Path childDataDir) {
        String relativePath = dataDir.relativize(childDataDir).normalize().toString();
        return FileNameExtract.extractPathName(relativePath);
    }

}
