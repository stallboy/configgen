package configgen.genjava.code;

import configgen.type.TBean;
import configgen.util.CachedIndentPrinter;

class GenBaseDynamicBeanInterface {

    static void generate(TBean tbean, BeanName name, CachedIndentPrinter ps) {
        ps.println("package %s;", name.pkg);
        ps.println();
        ps.println("public interface %s {", name.className);
        ps.inc();
        ps.println("%s type();", Name.fullName(tbean.getChildDynamicBeanEnumRefTable()));
        ps.println();

        if (tbean.hasRef()) {
            ps.println("default void _resolve(%s.ConfigMgr mgr) {", Name.codeTopPkg);
            ps.println("}");
            ps.println();
        }

        ps.println("static %s _create(configgen.genjava.ConfigInput input) {", name.className);
        ps.inc();
        ps.println("switch(input.readStr()) {");
        for (TBean actionBean : tbean.getChildDynamicBeans()) {
            ps.println1("case \"%s\":", actionBean.name);
            ps.println2("return %s._create(input);", Name.fullName(actionBean));
        }

        ps.println("}");
        ps.println("throw new IllegalArgumentException();");
        ps.dec();
        ps.println("}");
        ps.dec();
        ps.println("}");
    }
}
