package configgen.define;

import configgen.Node;
import configgen.util.DomUtils;
import org.w3c.dom.Element;

public class Import extends Node {
    public final String file;
    public final AllDefine define;

    public Import(AllDefine parent, Element self, String encoding) {
        super(parent, "import");
        file = self.getAttribute("file");

        define = new AllDefine(parent.resolvePath(file), encoding);
    }


    //////////////////////////////// extract
    private Import(Node _parent, Import original, String own) {
        super(_parent, "import");
        file = original.file;
        define = original.define.extract(own);
    }

    Import extract(Node _parent, String _own) {
        return new Import(_parent, this, _own);
    }

    void resolveExtract(AllDefine top) {
        define.resolveExtract(top);
    }


    //////////////////////////////// save
    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "import");
        self.setAttribute("file", file);
    }


}
