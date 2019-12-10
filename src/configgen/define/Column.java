package configgen.define;

import configgen.Node;
import configgen.util.DomUtils;
import org.w3c.dom.Element;

public class Column extends Node {

    public enum CompressType {
        // 没有压缩格子，其他2个类型都只占用1格
        NoCompress,
        // 用分割符的方案，分隔符自定义
        // 比如list,Bean类型，比如list用#分割，Bean为2个int组合配置用;分割，则单元格可配置为518;4#511;2114
        UseSeparator,
        // 这是个统一方案，不用在Bean上配置分割符号
        // 上面例子可配置为(518,4),(511,2114)
        // 这个方案支持嵌套循环的DynamicBean配置，比如：And(KillMonster(1001,2),Level(10))
        AsOne
    }

    public String desc;
    public final String type;
    public String own;


    public final CompressType compressType;

    public char compressSeparator;

    public ForeignKey foreignKey;
    public KeyRange keyRange;

    Column(Bean _parent, Element self) {
        super(_parent, self.getAttribute("name"));
        DomUtils.permitAttributes(self, "desc", "name", "type", "own",
                "ref", "refType", "keyRef", "range", "compress", "compressAsOne");
        desc = self.getAttribute("desc");
        type = self.getAttribute("type");
        own = self.getAttribute("own");

        if (self.hasAttribute("ref"))
            foreignKey = new ForeignKey(this, self);
        if (self.hasAttribute("range"))
            keyRange = new KeyRange(this, self);

        if (self.hasAttribute("compressAsOne")) {
            compressType = CompressType.AsOne;
        } else if (self.hasAttribute("compress")) {
            compressType = CompressType.UseSeparator;
            String sep = self.getAttribute("compress");
            require(sep.length() == 1, "compress字符串长度必须是1", sep);
            compressSeparator = sep.toCharArray()[0];
        } else {
            compressType = CompressType.NoCompress;
        }
    }

    Column(Bean _parent, String _name, String type, String desc) {
        super(_parent, _name);
        this.type = type;
        this.desc = desc;
        this.own = "";
        compressType = CompressType.NoCompress;
    }

    Column(Bean _parent, Column original) {
        super(_parent, original.name);
        desc = original.desc;
        type = original.type;
        own = original.own;
        if (original.foreignKey != null)
            foreignKey = new ForeignKey(this, original.foreignKey);
        if (original.keyRange != null)
            keyRange = new KeyRange(this, original.keyRange);

        compressType = original.compressType;
        compressSeparator = original.compressSeparator;
    }

    Column extract(Bean _parent, String _own) {
        if (own.contains(_own))
            return new Column(_parent, this);
        return null;
    }

    void resolveExtract() {
        if (foreignKey != null && foreignKey.invalid())
            foreignKey = null;
    }

    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "column");
        self.setAttribute("name", name);
        self.setAttribute("type", type);
        switch (compressType) {
            case UseSeparator:
                self.setAttribute("compress", String.valueOf(compressSeparator));
                break;
            case AsOne:
                self.setAttribute("compressAsOne", "1");
                break;
        }

        if (!desc.isEmpty())
            self.setAttribute("desc", desc);
        if (!own.isEmpty())
            self.setAttribute("own", own);

        if (foreignKey != null)
            foreignKey.update(self);
        if (keyRange != null)
            keyRange.update(self);
    }
}