package configgen.define;

import configgen.Node;
import configgen.util.DomUtils;
import configgen.view.DefineView;
import org.w3c.dom.Element;

import java.util.*;

public class Bean extends Node {

    public enum BeanType {
        /**
         * 正常Bean
         */
        NormalBean,
        /**
         * 对应表（csv）的Bean，bean知道它直接被用于table了，也许不应该。
         */
        Table,
        /**
         * 多态Bean的基类
         * 这里只支持一层的继承-多态。不支持多层，如果需要的话用组合来做吧
         */
        BaseDynamicBean,
        /**
         * 多态Bean的具体子类
         */
        ChildDynamicBean,
    }

    Define define;
    public final BeanType type;
    public final String own;

    //对应Column.PackType.UseSeparator,之后建议column配置用AsOne，这里就不需要了。
    public final boolean isPackBySeparator;
    public final char packSeparator;

    public final Map<String, Column> columns = new LinkedHashMap<>();  //列
    public final Map<String, ForeignKey> foreignKeys = new LinkedHashMap<>(); //外键
    public final Map<String, KeyRange> ranges = new LinkedHashMap<>(); //额外约束

    //多态Bean基类包含这些子类定义
    public final String childDynamicBeanEnumRef;
    public final String childDynamicDefaultBeanName;
    public final Map<String, Bean> childDynamicBeans = new LinkedHashMap<>();



    /**
     * 正常Bean 或 多态基类Bean
     */
    Bean(Define _parent, Element self) {
        super(_parent, _parent.wrapPkgName(self.getAttribute("name")));
        define = _parent;

        require(self.hasAttribute("name"), "bean必须设置name");
        require(!name.isEmpty(), "name不能是空字符串");

        own = self.getAttribute("own");

        if (self.hasAttribute("compress")) { // 改为packSep吧
            isPackBySeparator = true;
            String sep = self.getAttribute("compress");
            require(sep.length() == 1, "分隔符compress长度必须为1");
            packSeparator = sep.toCharArray()[0];
        } else if (self.hasAttribute("packSep")) {
            isPackBySeparator = true;
            String sep = self.getAttribute("packSep");
            require(sep.length() == 1, "分隔符pack长度必须为1");
            packSeparator = sep.toCharArray()[0];
        } else {
            isPackBySeparator = false;
            packSeparator = ';';
        }
        childDynamicBeanEnumRef = self.getAttribute("enumRef");
        childDynamicDefaultBeanName = self.getAttribute("defaultBeanName");
        if (self.hasAttribute("enumRef")) {
            type = BeanType.BaseDynamicBean;
            DomUtils.permitAttributes(self, "name", "own", "enumRef", "defaultBeanName");
            DomUtils.permitElements(self, "bean");
            for (Element e : DomUtils.elements(self, "bean")) {
                Bean b = new Bean(this, e);
                require(null == childDynamicBeans.put(b.name, b), "Bean名字重复", b.name);
            }
            if (!childDynamicDefaultBeanName.isEmpty()) {
                Bean childDynamicDefaultBean = childDynamicBeans.get(childDynamicDefaultBeanName);
                require(Objects.nonNull(childDynamicDefaultBean), "defaultBeanName未定义", childDynamicDefaultBeanName);
                require(childDynamicDefaultBean.columns.isEmpty(), "defaultBean不能定义column",
                        childDynamicDefaultBeanName);
            }
        } else {
            type = BeanType.NormalBean;
            DomUtils.permitAttributes(self, "name", "own", "compress", "packSep");
            DomUtils.permitElements(self, "column", "foreignKey", "keyRange");
            init(self);
        }
    }

    /**
     * 作为Table的Bean
     */
    Bean(Table _parent, Define _define, Element self) {
        super(_parent, _define.wrapPkgName(self.getAttribute("name")));
        define = _define;
        own = self.getAttribute("own");
        type = BeanType.Table;
        isPackBySeparator = false;
        packSeparator = ';';
        childDynamicBeanEnumRef = "";
        childDynamicDefaultBeanName = "";
        init(self);
    }

    /**
     * 子Bean
     */
    private Bean(Bean _parent, Element self) {
        super(_parent, self.getAttribute("name")); //子bean的名称不用包装pkgName

        define = _parent.define;
        own = self.getAttribute("own");
        require(_parent.type == BeanType.BaseDynamicBean, "不允许ChildDynamicBean又有ChildDynamicBean");
        type = BeanType.ChildDynamicBean;
        isPackBySeparator = _parent.isPackBySeparator;
        packSeparator = _parent.packSeparator;
        childDynamicBeanEnumRef = "";
        childDynamicDefaultBeanName = "";
        DomUtils.permitAttributes(self, "name", "own");
        DomUtils.permitElements(self, "column", "foreignKey", "keyRange");
        init(self);
    }

    private void init(Element self) {
        for (Element ele : DomUtils.elements(self, "column")) {
            Column c = new Column(this, ele);
            require(null == columns.put(c.name, c), "列名字定义重复", c.name);
        }

        for (Element ele : DomUtils.elements(self, "foreignKey")) {
            ForeignKey fk = new ForeignKey(this, ele);
            require(null == foreignKeys.put(fk.name, fk), "外键名字定义重复", fk.name);
        }

        for (Element ef : DomUtils.elements(self, "keyRange")) {
            KeyRange r = new KeyRange(this, ef);
            require(null == ranges.put(r.key, r), "keyRange定义重复", r.key);
        }
    }


    /**
     * 有新csv文件，导致的新建Bean
     */
    Bean(Table table, Define _define, String name) {
        super(table, name);
        define = _define;
        type = BeanType.Table;
        childDynamicBeanEnumRef = "";
        childDynamicDefaultBeanName = "";
        own = "";
        isPackBySeparator = false;
        packSeparator = ';';
    }


    @Override
    public String fullName() {
        if (parent instanceof Table) {
            return parent.fullName();
        } else {
            return parent.fullName() + "." + name;
        }
    }

    public String getPkgName() {
        String pkgAndName;
        if (type == Bean.BeanType.ChildDynamicBean) {
            pkgAndName = parent.name;
        } else {
            pkgAndName = name;
        }
        int i = pkgAndName.lastIndexOf(".");
        return i < 0 ? "" : pkgAndName.substring(0, i);
    }

    public Define getDefine() {
        return define;
    }

    public Column getColumn(String col){
        return columns.get(col);
    }


    //////////////////////////////// extract

    /**
     * 抽取部分字段作为新的Bean，因为有些字段客户端不需要，或者说不想泄露到客户端包里
     */
    private Bean(Node _parent, Bean original) {
        super(_parent, original.name);
        define = original.define;
        type = original.type;
        childDynamicBeanEnumRef = original.childDynamicBeanEnumRef;
        childDynamicDefaultBeanName = original.childDynamicDefaultBeanName;
        own = original.own;
        isPackBySeparator = original.isPackBySeparator;
        packSeparator = original.packSeparator;
    }

    Bean extract(Node _parent, DefineView defineView) {
        Bean part = new Bean(_parent, this);
        if (type == BeanType.BaseDynamicBean) {
            // 对于多态Bean,只用在基类上配置own，不需要在每个子Bean上都配置own
            childDynamicBeans.forEach((name, actionBean) -> {
                Bean bn = actionBean.extract(part, defineView);
                part.childDynamicBeans.put(name, bn);
            });
        } else {
            // 标记了own的列 提取出来
            columns.forEach((name, c) -> {
                if (defineView.filter.acceptColumn(c)) {
                    Column pc = c.extract(part);
                    part.columns.put(name, Objects.requireNonNull(pc));
                }
            });

            ranges.forEach((n, r) -> {
                if (part.columns.containsKey(n))
                    part.ranges.put(n, new KeyRange(part, r));
            });

            foreignKeys.forEach((n, fk) -> {
                if (part.columns.keySet().containsAll(Arrays.asList(fk.keys)))
                    part.foreignKeys.put(n, new ForeignKey(part, fk));
            });
        }
        return part;
    }

    public void resolveExtract(DefineView defineView) {
        if (type == BeanType.BaseDynamicBean) {
            for (Bean actionBean : childDynamicBeans.values()) {
                actionBean.resolveExtract(defineView);
            }
        }

        for (Column col : columns.values()) {
            col.resolveExtract(defineView);
        }

        List<String> dels = new ArrayList<>();
        foreignKeys.forEach((n, fk) -> {
            if (fk.invalid(this, defineView)) {
                dels.add(n);
            }
        });
        for (String del : dels) {
            foreignKeys.remove(del);
        }
    }


    //////////////////////////////// auto fix
    public void autoFixDefine(AllDefine defineToFix) {
        for (Column col : columns.values()) {
            if (col.foreignKey != null) {
                col.foreignKey.autoFixDefine(this, defineToFix);
            }
        }

        for (ForeignKey fk : foreignKeys.values()) {
            fk.autoFixDefine(this, defineToFix);
        }
    }

    public void verifyDefine(AllDefine fullDefine) {
        for (Column col : columns.values()) {
            if (col.foreignKey != null) {
                col.foreignKey.verifyDefine(this, fullDefine);
            }
        }

        for (ForeignKey fk : foreignKeys.values()) {
            fk.verifyDefine(this, fullDefine);
        }
    }


    //////////////////////////////// save
    void save(Element parent) {
        update(DomUtils.newChild(parent, "bean"));
    }

    void update(Element self) {
        if (type == BeanType.ChildDynamicBean)
            self.setAttribute("name", name);
        else
            self.setAttribute("name", define.unwrapPkgName(name));
        if (!own.isEmpty())
            self.setAttribute("own", own);
        if (isPackBySeparator)
            self.setAttribute("packSep", String.valueOf(packSeparator));
        if (!childDynamicBeanEnumRef.isEmpty())
            self.setAttribute("enumRef", childDynamicBeanEnumRef);
        if (!childDynamicDefaultBeanName.isEmpty())
            self.setAttribute("defaultBeanName", childDynamicDefaultBeanName);

        columns.values().forEach(c -> c.save(self));
        foreignKeys.values().forEach(c -> c.save(self));
        ranges.values().forEach(c -> c.save(self));
        childDynamicBeans.values().forEach(c -> c.save(self));
    }
}