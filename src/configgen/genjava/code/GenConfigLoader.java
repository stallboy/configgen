package configgen.genjava.code;

import configgen.util.CachedIndentPrinter;

class GenConfigLoader {

    static void generate(CachedIndentPrinter ps){
        ps.println("package %s;", Name.codeTopPkg);
        ps.println();

        ps.println("public interface ConfigLoader {");
        ps.println();

        ps.println1("void createAll(ConfigMgr mgr, configgen.genjava.ConfigInput input);");
        ps.println();

        ps.println1("void resolveAll(ConfigMgr mgr);");
        ps.println();

        ps.println("}");
    }
}
