package configgen.define;

import configgen.Node;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class Table extends Node {
    public Bean bean;
    public String enumStr;
    public String[] primaryKey;
    public final Map<String, UniqueKey> uniqueKeys = new LinkedHashMap<>();

    public Table(Db parent, Element self) {
        super(parent, self.getAttribute("name"));
        DomUtils.permitAttributes(self, "name", "own", "enum", "primaryKey");
        DomUtils.permitElements(self, "column", "foreignKey", "range", "uniqueKey");

        bean = new Bean(this, self);
        enumStr = self.getAttribute("enum");
        if (self.hasAttribute("primaryKey")) {
            primaryKey = DomUtils.parseStringArray(self, "primaryKey");
        } else {
            primaryKey = new String[]{bean.columns.keySet().iterator().next()};
        }

        for (Element ele : DomUtils.elements(self, "uniqueKey")) {
            UniqueKey uk = new UniqueKey(this, ele);
            require(!Arrays.equals(uk.keys, primaryKey), "uniqueKey duplicate primaryKey", uk.toString());
            UniqueKey old = uniqueKeys.put(uk.toString(), uk);
            require(old == null, "uniqueKey duplicate", uk.toString());
        }
    }

    Table(Db parent, String name) {
        super(parent, name);
        bean = new Bean(this, name);
        enumStr = "";
        primaryKey = new String[0];
    }

    private Table(Db _parent, Table original) {
        super(_parent, original.name);
        enumStr = original.enumStr;
        primaryKey = original.primaryKey;
    }

    Table extract(Db _parent, String own) {
        Table part = new Table(_parent, this);
        part.bean = bean.extract(part, own);
        if (part.bean == null)
            return null;

        uniqueKeys.forEach((n, uk) -> {
            if (part.bean.columns.keySet().containsAll(Arrays.asList(uk.keys))) {
                part.uniqueKeys.put(n, new UniqueKey(part, uk));
            }
        });
        return part;
    }

    void resolveExtract() {
        bean.resolveExtract();
        String original = enumStr;
        enumStr = bean.columns.containsKey(original) ? original : "";
        require(bean.columns.keySet().containsAll(Arrays.asList(primaryKey)), "must own primaryKey");
    }

    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "table");
        uniqueKeys.values().forEach(c -> c.save(self));
        bean.update(self);
        if (!enumStr.isEmpty())
            self.setAttribute("enum", enumStr);
        if (primaryKey.length > 0)
            self.setAttribute("primaryKey", String.join(",", primaryKey));
    }
}
