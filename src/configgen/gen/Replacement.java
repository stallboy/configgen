package configgen.gen;

import configgen.util.DomUtils;
import org.w3c.dom.Element;

import java.nio.file.Paths;
import java.util.HashMap;

public class Replacement {

    private final HashMap<String, String> src2dst = new HashMap<>();

    public Replacement(String filePath) {
        Element root = DomUtils.rootElement(Paths.get(filePath).toFile());
        for (Element rep : DomUtils.elements(root, "rep")) {
            String src = rep.getAttribute("src");
            String dst = rep.getAttribute("dst");
            src2dst.put(src, dst);
        }
    }

    public String replace(String ori) {
        final String[] target = {ori};
        src2dst.forEach((src, dst) -> {
            target[0] = target[0].replace(src, dst);
        });
        return target[0];
    }

}
