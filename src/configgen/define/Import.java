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
    void extract(DefineView defineView, String own) {
        define.extract(defineView, own);
    }


    //////////////////////////////// save
    void save(Element parent) {
        Element self = DomUtils.newChild(parent, "import");
        self.setAttribute("file", file);
    }


}
