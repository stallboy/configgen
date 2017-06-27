package configgen.define;

import configgen.Node;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class Table extends Node {
    public enum EnumType {
        None,
        EnumFull,
        EnumPart,
    }

    public Bean bean;
    public EnumType enumType;
    public String enumStr;
    public String[] primaryKey;
    public final Map<String, UniqueKey> uniqueKeys = new LinkedHashMap<>();

    public Table(Db parent, Element self) {
        super(parent, self.getAttribute("name"));
        DomUtils.permitAttributes(self, "name", "own", "enum", "enumPart", "primaryKey");
        DomUtils.permitElements(self, "column", "foreignKey", "range", "uniqueKey");

        bean = new Bean(this, self);
        enumType = EnumType.None;
        enumStr = "";
        if (self.hasAttribute("enum")) {
            enumType = EnumType.EnumFull;
            enumStr = self.getAttribute("enum");
            require(!enumStr.isEmpty(), "enum empty");
            require(!self.hasAttribute("enumPart"), "enum and enumPart conflict");
        } else if (self.hasAttribute("enumPart")) {
            enumType = EnumType.EnumPart;
            enumStr = self.getAttribute("enumPart");
            require(!enumStr.isEmpty(), "enumPart empty");
        }

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

    public boolean isEnum() {
        return enumType != EnumType.None;
    }

    public boolean isEnumAsPrimaryKey() {
        return primaryKey.length == 1 && primaryKey[0].equals(enumStr);
    }

    public boolean isEnumHasOnlyPrimaryKeyAndEnumStr() {
        if (enumType != EnumType.None) {
            if (isEnumAsPrimaryKey()) {
                return bean.columns.size() == 1;
            } else {
                return bean.columns.size() == 2;
            }
        }
        return false;
    }

    Table(Db parent, String name) {
        super(parent, name);
        bean = new Bean(this, name);
        enumType = EnumType.None;
        enumStr = "";
        primaryKey = new String[0];
    }

    private Table(Db _parent, Table original) {
        super(_parent, original.name);
        enumType = original.enumType;
        enumStr = original.enumStr;
        primaryKey = original.primaryKey;
    }

    void checkInclude(Table stable) {
        bean.checkInclude(stable.bean);
        require(Arrays.equals(primaryKey, stable.primaryKey), "primaryKey check include err");
        stable.uniqueKeys.forEach((n, uk) -> {
            require(uniqueKeys.containsKey(n), "uniqueKey check include err");
        });
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
        if (!bean.columns.containsKey(original)) {
            enumType = EnumType.None;
            enumStr = "";
        }
        require(bean.columns.keySet().containsAll(Arrays.asList(primaryKey)), "must own primaryKey");
    }

    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "table");
        uniqueKeys.values().forEach(c -> c.save(self));
        bean.update(self);
        switch (enumType) {
            case None:
                break;
            case EnumFull:
                self.setAttribute("enum", enumStr);
                break;
            case EnumPart:
                self.setAttribute("enumPart", enumStr);
                break;
        }
        if (primaryKey.length > 0) {
            self.setAttribute("primaryKey", String.join(",", primaryKey));
        }
    }
}
