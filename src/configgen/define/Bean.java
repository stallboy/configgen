package configgen.define;

import configgen.Node;
import configgen.util.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

public class Bean extends Node {
    public enum BeanType {
        NormalBean,         //Bean
        Table,              //表
        BaseDynamicBean,    //多态Bean的基类
        ChildDynamicBean,   //多态Bean的具体子类
    }

    public final BeanType type;
    private final String own;
    public final boolean compress;
    public final char compressSeparator;

    public final Map<String, Column> columns = new LinkedHashMap<>();  //列
    public final Map<String, ForeignKey> foreignKeys = new LinkedHashMap<>(); //外键
    public final Map<String, KeyRange> ranges = new LinkedHashMap<>(); //额外约束

    //多态Bean基类包含这些子类定义
    public final String childDynamicBeanEnumRef;
    public final Map<String, Bean> childDynamicBeans = new LinkedHashMap<>();
    

    Bean(Db _parent, Element self) {
        super(_parent, self.getAttribute("name"));
        own = self.getAttribute("own");

        compress = self.hasAttribute("compress");
        if (compress) {
            String sep = self.getAttribute("compress");
            require(sep.length() == 1, "分隔符compress长度必须为1");
            compressSeparator = sep.toCharArray()[0];
        } else {
            compressSeparator = ';';
        }
        childDynamicBeanEnumRef = self.getAttribute("enumRef");
        if (self.hasAttribute("enumRef")) {
            type = BeanType.BaseDynamicBean;
            DomUtils.permitAttributes(self, "name", "own", "enumRef");
            DomUtils.permitElements(self, "bean");
            for (Element e : DomUtils.elements(self, "bean")) {
                Bean b = new Bean(this, e);
                require(null == childDynamicBeans.put(b.name, b), "Bean名字重复", b.name);
            }
        } else {
            type = BeanType.NormalBean;
            DomUtils.permitAttributes(self, "name", "own", "compress");
            DomUtils.permitElements(self, "column", "foreignKey", "keyRange");
            init(self);
        }
    }

    Bean(Table _parent, Element self) {
        super(_parent, self.getAttribute("name"));
        own = self.getAttribute("own");
        type = BeanType.Table;
        compress = false;
        compressSeparator = ';';
        childDynamicBeanEnumRef = "";
        init(self);
    }

    private Bean(Bean _parent, Element self) {
        super(_parent, self.getAttribute("name"));
        own = self.getAttribute("own");
        type = BeanType.ChildDynamicBean;
        compress = false;
        childDynamicBeanEnumRef = "";
        compressSeparator = ';';
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

    Bean(Table table, String name) {
        super(table, name);
        type = BeanType.Table;
        childDynamicBeanEnumRef = "";
        own = "";
        compress = false;
        compressSeparator = ';';
    }

    private Bean(Node _parent, Bean original) {
        super(_parent, original.name);
        type = original.type;
        childDynamicBeanEnumRef = original.childDynamicBeanEnumRef;
        own = original.own;
        compress = original.compress;
        compressSeparator = original.compressSeparator;
    }

    Bean extract(Node _parent, String _own) {
        Bean part = new Bean(_parent, this);

        if (type == BeanType.BaseDynamicBean) {
            if (!own.contains(_own))
                return null;
            childDynamicBeans.forEach((name, actionBean) -> {
                Bean bn = actionBean.extract(part, "_do_not_set_own_on_action_");
                if (bn != null)
                    part.childDynamicBeans.put(name, bn);
            });
        } else {
            columns.forEach((name, c) -> {
                Column pc = c.extract(part, _own);
                if (pc != null)
                    part.columns.put(name, pc);
            });
            if (part.columns.isEmpty() && (own.contains(_own) || type == BeanType.ChildDynamicBean)) {
                columns.forEach((name, f) -> part.columns.put(name, new Column(part, f)));
            }
            if (part.columns.isEmpty())
                return null;

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

    void resolveExtract() {
        columns.values().forEach(Column::resolveExtract);
        List<String> dels = new ArrayList<>();
        foreignKeys.forEach((n, fk) -> {
            if (fk.invalid()) {
                dels.add(n);
            }
        });
        for (String del : dels) {
            foreignKeys.remove(del);
        }
    }

    void save(Element parent) {
        update(DomUtils.newChild(parent, "bean"));
    }

    void update(Element self) {
        self.setAttribute("name", name);
        if (!own.isEmpty())
            self.setAttribute("own", own);
        if (compress)
            self.setAttribute("compress", String.valueOf(compressSeparator));
        if (!childDynamicBeanEnumRef.isEmpty())
            self.setAttribute("enumRef", childDynamicBeanEnumRef);

        columns.values().forEach(c -> c.save(self));
        foreignKeys.values().forEach(c -> c.save(self));
        ranges.values().forEach(c -> c.save(self));
        childDynamicBeans.values().forEach(c -> c.save(self));
    }
}