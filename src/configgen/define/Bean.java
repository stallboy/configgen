package configgen.define;

import configgen.Node;
import configgen.util.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

public class Bean extends Node {
    public enum BeanType {
        NormalBean,
        Table,
        BaseAction,
        Action,
    }

    public final BeanType type;
    private final String own;
    public final boolean compress;
    public final char compressSeparator;

    public final Map<String, Column> columns = new LinkedHashMap<>();
    public final Map<String, ForeignKey> foreignKeys = new LinkedHashMap<>();
    public final Map<String, KeyRange> ranges = new LinkedHashMap<>();

    public final String actionEnumRef;
    public final Map<String, Bean> actionBeans = new LinkedHashMap<>();

    Bean(Db _parent, Element self) {
        super(_parent, self.getAttribute("name"));
        own = self.getAttribute("own");

        compress = self.hasAttribute("compress");
        if (compress) {
            String sep = self.getAttribute("compress");
            require(sep.length() == 1, "compress separator length not 1, separator=" + sep);
            compressSeparator = sep.toCharArray()[0];
        } else {
            compressSeparator = ';';
        }
        actionEnumRef = self.getAttribute("enumRef");
        if (self.hasAttribute("enumRef")) {
            type = BeanType.BaseAction;
            DomUtils.permitAttributes(self, "name", "own", "enumRef");
            DomUtils.permitElements(self, "bean");
            for (Element e : DomUtils.elements(self, "bean")) {
                Bean b = new Bean(this, e);
                require(null == actionBeans.put(b.name, b), "bean duplicate name=" + b.name);
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
        actionEnumRef = "";
        init(self);
    }

    private Bean(Bean _parent, Element self) {
        super(_parent, self.getAttribute("name"));
        own = self.getAttribute("own");
        type = BeanType.Action;
        compress = false;
        actionEnumRef = "";
        compressSeparator = ';';
        DomUtils.permitAttributes(self, "name", "own");
        DomUtils.permitElements(self, "column", "foreignKey", "keyRange");
        init(self);
    }

    private void init(Element self) {
        for (Element ele : DomUtils.elements(self, "column")) {
            Column c = new Column(this, ele);
            require(null == columns.put(c.name, c), "column duplicate name=" + c.name);
        }

        for (Element ele : DomUtils.elements(self, "foreignKey")) {
            ForeignKey fk = new ForeignKey(this, ele);
            require(null == foreignKeys.put(fk.name, fk), "ForeignKey duplicate name=" + fk.name);
        }

        for (Element ef : DomUtils.elements(self, "keyRange")) {
            KeyRange r = new KeyRange(this, ef);
            require(null == ranges.put(r.key, r), "keyRange duplicate key=" + r.key);
        }
    }

    Bean(Table table, String name) {
        super(table, name);
        type = BeanType.Table;
        actionEnumRef = "";
        own = "";
        compress = false;
        compressSeparator = ';';
    }

    private Bean(Node _parent, Bean original) {
        super(_parent, original.name);
        type = original.type;
        actionEnumRef = original.actionEnumRef;
        own = original.own;
        compress = original.compress;
        compressSeparator = original.compressSeparator;
    }

    Bean extract(Node _parent, String _own) {
        Bean part = new Bean(_parent, this);

        if (type == BeanType.BaseAction) {
            if (!own.contains(_own))
                return null;
            actionBeans.forEach((name, actionBean) -> {
                Bean bn = actionBean.extract(part, "_do_not_set_own_on_action_");
                if (bn != null)
                    part.actionBeans.put(name, bn);
            });
        } else {
            columns.forEach((name, c) -> {
                Column pc = c.extract(part, _own);
                if (pc != null)
                    part.columns.put(name, pc);
            });
            if (part.columns.isEmpty() && (own.contains(_own) || type == BeanType.Action)) {
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
        if (!actionEnumRef.isEmpty())
            self.setAttribute("enumRef", actionEnumRef);

        columns.values().forEach(c -> c.save(self));
        foreignKeys.values().forEach(c -> c.save(self));
        ranges.values().forEach(c -> c.save(self));
        actionBeans.values().forEach(c -> c.save(self));
    }
}