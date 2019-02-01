package configgen.genjava.code;

import configgen.define.Bean;
import configgen.type.TBean;

import java.util.Arrays;

class BeanName {
    final String pkg;
    final String className;
    final String fullName;
    final String path;
    final String containerPrefix;

    BeanName(TBean tbean) {
        this(tbean, "");
    }

    BeanName(TBean tbean, String postfix) {
        String topPkg = Name.codeTopPkg;
        String name;
        if (tbean.getBeanDefine().type == Bean.BeanType.ChildDynamicBean) {
            TBean baseAction = (TBean) tbean.parent;
            name = baseAction.name.toLowerCase() + "." + tbean.name;
        } else {
            name = tbean.name;
        }

        name += postfix;
        containerPrefix = tbean.name.replace('.', '_') + "_";
        String[] seps = name.split("\\.");
        String c = seps[seps.length - 1];
        className = c.substring(0, 1).toUpperCase() + c.substring(1);

        String[] pks = Arrays.copyOf(seps, seps.length - 1);
        if (pks.length == 0)
            pkg = topPkg;
        else
            pkg = topPkg + "." + String.join(".", pks);

        fullName = pkg + "." + className;
        if (pks.length == 0)
            path = className + ".java";
        else
            path = String.join("/", pks) + "/" + className + ".java";

    }
}
