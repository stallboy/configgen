package configgen.genjava.code;

import configgen.util.CachedIndentPrinter;

class GenConfigMgr {

    static void generate(CachedIndentPrinter ps){
        ps.println("package %s;", Name.codeTopPkg);
        ps.println();

        ps.println("public class ConfigMgr {");

        ps.println1("private static volatile ConfigMgr mgr;");
        ps.println();
        ps.println1("public static ConfigMgr getMgr(){");
        ps.println2("return mgr;");
        ps.println1("}");
        ps.println();

        ps.println1("public static void setMgr(ConfigMgr newMgr){");
        ps.println2("mgr = newMgr;");
        ps.println1("}");
        ps.println();


        for (String s : GenBeanClassTablePart.mapsInMgr) {
            ps.println(s);
            ps.println();
        }

        ps.println("}");
    }
}
