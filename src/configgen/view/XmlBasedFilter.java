package configgen.view;

import configgen.define.AllDefine;
import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.Table;
import configgen.util.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * 根据view_xxx.xml配置定义的过滤器
 */
public class XmlBasedFilter extends AbstractFilter {
    private final Path viewXmlPath;
    private final EDefault eDefault; // 未定义的pkg默认行为
    private final boolean simplify; // 是否简化此xml内容中的include和exclude定义，默认false
    private final String own;
    private final Set<String> includes = new TreeSet<>();
    private final Set<String> excludes = new TreeSet<>();

    public XmlBasedFilter(Path viewXmlPath) {
        this(viewXmlPath.toAbsolutePath().normalize(), new LinkedHashSet<>());
    }

    public XmlBasedFilter(Path viewXmlPath, Set<Path> processed) {
        processed.add(viewXmlPath);
        this.viewXmlPath = viewXmlPath;
        if (!Files.exists(viewXmlPath)) {
            eDefault = EDefault.Include;
            simplify = true;
            own = null;
            return;
        }

        Element self = DomUtils.rootElement(viewXmlPath.toFile());
        if (!"view".equals(self.getNodeName())) {
            throw new AssertionError("viewXml的跟节点必须是：view，当前为：" + self.getNodeName() + ", file=" + viewXmlPath);
        }
        DomUtils.permitAttributes(self, "default", "own", "simplify");

        eDefault = EDefault.readFrom(self);
        simplify = "true".equalsIgnoreCase(self.getAttribute("simplify"));
        own = self.getAttribute("own");
        if (own != null && !own.isEmpty()) {
            OwnFilter ownFilter = new OwnFilter(own);
            setParent(ownFilter);
        }

        DomUtils.permitElements(self, "include", "exclude");
        for (Element e : DomUtils.elements(self, "include")) {
            DomUtils.permitAttributes(e, "pkg");
            String pkg = e.getAttribute("pkg");
            includes.add(pkg);
        }
        for (Element e : DomUtils.elements(self, "exclude")) {
            DomUtils.permitAttributes(e, "pkg");
            String pkg = e.getAttribute("pkg");
            excludes.add(pkg);
        }
        Set<String> intersect = new TreeSet<>(includes);
        intersect.retainAll(excludes);
        if (!intersect.isEmpty()) {
            throw new AssertionError("include 和 exclude存在交集. intersect=" + intersect);
        }
    }

    @Override
    protected void doSaveToXml(AllDefine allDefine) {
        Set<String> pkgs = new TreeSet<>();
        for (Bean bean : allDefine.getAllBeans()) {
            String pkg = nameToPkg(bean.name);
            pkgs.add(pkg);
        }
        for (Table table : allDefine.getAllTables()) {
            String pkg = nameToPkg(table.name);
            pkgs.add(pkg);
        }

        for (String pkg : pkgs) {
            if (includes.contains(pkg) || excludes.contains(pkg)) {
                continue;
            }
            if (eDefault == EDefault.Include) {
                includes.add(pkg);
            } else {
                excludes.add(pkg);
            }
        }

        if (simplify) {
            if (eDefault == EDefault.Include) {
                includes.clear();
            } else {
                excludes.clear();
            }
        }

        Document doc = DomUtils.newDocument();

        Node commentNode = doc.createComment(commentText);
        doc.appendChild(commentNode);

        Element self = doc.createElement("view");
        doc.appendChild(self);

        EDefault.saveTo(self, eDefault);
        self.setAttribute("simplify", Boolean.toString(simplify));
        if (own != null && !own.isEmpty()) {
            self.setAttribute("own", own);
        }

        for (String pkg : includes) {
            Element include = DomUtils.newChild(self, "include");
            include.setAttribute("pkg", pkg);
        }

        for (String pkg : excludes) {
            Element excludes = DomUtils.newChild(self, "exclude");
            excludes.setAttribute("pkg", pkg);
        }

        DomUtils.prettySaveDocument(doc, viewXmlPath.toFile(), allDefine.getEncoding());
    }

    @Override
    public String name() {
        return viewXmlPath.toFile().getName();
    }


    @Override
    protected boolean doAcceptColumn(Column column) {
        return true;
    }

    @Override
    protected boolean doAcceptBean(Bean bean) {
        return true;
    }

    @Override
    protected boolean doAcceptTable(Table table) {
        // 目前只支持pkg级别的过滤，有需求再细分吧
        String pkgName = nameToPkg(table.name);

        return acceptPkg(pkgName);
    }

    private String nameToPkg(String name) {
        int i = name.lastIndexOf('.');

        return i <= 0 ? "" : name.substring(0, i);
    }

    private boolean acceptPkg(String pkg) {
        if (eDefault == EDefault.Include) {
            return !excludes.contains(pkg);
        } else {
            return includes.contains(pkg);
        }
    }

    /** 未定义的pkg默认行为 */
    enum EDefault {
        Include,
        Exclude,
        ;

        static EDefault readFrom(Element self) {
            if (!self.hasAttribute("default")) {
                return Include;
            }

            String defaultStr = self.getAttribute("default");
            for (EDefault tmp : EDefault.values()) {
                if (tmp.name().equalsIgnoreCase(defaultStr)) {
                    return tmp;
                }
            }
            if (!defaultStr.isEmpty()) {
                throw new AssertionError("default只支持:[Include, Exclude], 当前为：" + defaultStr);
            }
            return null;
        }

        static void saveTo(Element self, EDefault val) {
            if (val != null) {
                self.setAttribute("default", val.name().toLowerCase());
            }
        }
    }

    String commentText = "\n" +
            "view：视图，名字参考关系型数据库中的view\n" +
            "\n" +
            "default：未指定的pkg默认行为，默认include\n" +
            "\tinclude: 未定义pkg默认include\n" +
            "\texclude: 未定义行为默认exclude\n" +
            "\n" +
            "simplify：是否简化此xml内容中的include和exclude定义，默认false\n" +
            "\tfalse：include和exclude全部放到此xml中\n" +
            "\ttrue：default==include时，此xml中只会有exclude定义。 default==exclude时，此xml中只会有include定义" +
            "\n";
}
