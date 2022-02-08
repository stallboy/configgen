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

public class Define extends Node {
    public final AllDefine allDefine;
    public final String file;
    public final String pkgName;
    final SplitMode splitMode;

    final Map<String, Bean> beans = new TreeMap<>();
    final Map<String, Table> tables = new TreeMap<>();

    Define(AllDefine parent, String file) {
        super(parent, "define");

        this.allDefine = parent;
        this.file = file;
        Path defineXmlPath = parent.resolvePath(file);
        this.pkgName = parent.childDataPathToPkgName(defineXmlPath.getParent());

        if (!Files.exists(defineXmlPath)) {
            splitMode = SplitMode.AllInOne;
            return;
        }

        Element self = DomUtils.rootElement(defineXmlPath.toFile());
        DomUtils.permitAttributes(self, "splitMode"); //datadir规定必须是本目录
        splitMode = SplitMode.readFrom(self);

        DomUtils.permitElements(self, "bean", "table");

        for (Element e : DomUtils.elements(self, "bean")) {
            Bean b = new Bean(this, e);
            require(null == beans.put(b.name, b), "Bean定义名字重复", b.name);
        }

        for (Element e : DomUtils.elements(self, "table")) {
            Table t = new Table(this, e);
            require(null == tables.put(t.name, t), "表定义名字重复", t.name);
            require(!beans.containsKey(t.name), "表和Bean定义名字重复", t.name);
        }
    }

    boolean canDelete() {
        // top define中有splitMode定义，所以不能删除
        return beans.isEmpty() && tables.isEmpty() && !allDefine.isTopDefine(this);
    }

    void saveToXml(String encoding) {
        Path defineXmlPath = allDefine.resolvePath(file);
        if (canDelete()) {
            try {
                if (Files.deleteIfExists(defineXmlPath)) {
                    System.out.println("Delete File: " + defineXmlPath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        Document doc = DomUtils.newDocument();

        Element self = doc.createElement("db");
        doc.appendChild(self);
        if (allDefine.isTopDefine(this)) {
            splitMode.saveTo(self);
        }
        //self.setAttribute("datadir", "."); 约定只能在datadir同目录，所以不要这个属性了

        for (Bean b : beans.values()) {
            b.save(self);
        }
        for (Table t : tables.values()) {
            t.save(self);
        }

        DomUtils.prettySaveDocument(doc, defineXmlPath.toFile(), encoding);
    }

    // 纠正bean和table所属的define
    void rectify() {
        for (Bean b : beans.values()) {
            b.define = this;
        }
        for (Table t : tables.values()) {
            t.bean.define = this;
        }
    }

    String unwrapPkgName(String fullName) {
        if (pkgName.isEmpty() || pkgName.equals(".")) {
            return fullName;
        }
        require(fullName.startsWith(pkgName + "."), "Bean或者Table的名称和pkg名称不匹配, fullName=" + fullName + ", pkg=" + pkgName);

        String localName = fullName.substring(pkgName.length() + 1);
        require(!localName.contains("."), "去除包名错误. name=" + localName + "fullName=" + fullName + ", pkg=" + pkgName);

        return localName;
    }

    public String wrapPkgName(String localName) {
        if (name.startsWith(".")) {
            return name.substring(1);
        }
        if (name.contains(".")) { //已经是globalName了
            return name;
        }

        if (pkgName.isEmpty() || pkgName.equals(".")) {
            return localName;
        }
        return pkgName + "." + localName;
    }

    enum SplitMode {
        /** 所有bean和table的定义都在同一个xml文件中 */
        AllInOne,
        /** 基于包的分割，将bean和table按包名分割到不同文件夹下的xml中 */
        PkgBased,
        ;

        static SplitMode readFrom(Element self) {
            if (!self.hasAttribute("splitMode")) {
                return AllInOne;
            }

            String splitModeStr = self.getAttribute("splitMode");
            for (SplitMode tmp : SplitMode.values()) {
                if (tmp.name().equalsIgnoreCase(splitModeStr)) {
                    return tmp;
                }
            }
            if (!splitModeStr.isEmpty()) {
                throw new AssertionError("splitMode只支持:[AllInOne, PkgBased], 当前为：" + splitModeStr);
            }
            return AllInOne;
        }

        void saveTo(Element self) {
            self.setAttribute("splitMode", name());
        }
    }
}
