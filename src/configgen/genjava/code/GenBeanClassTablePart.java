package configgen.genjava.code;

import configgen.gen.Generator;
import configgen.type.TBean;
import configgen.type.TTable;
import configgen.type.Type;
import configgen.util.CachedIndentPrinter;
import configgen.value.VTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class GenBeanClassTablePart {

    static List<String> mapsInMgr = new ArrayList<>();


    static void generate(TBean tbean, VTable vtable, BeanName name, CachedIndentPrinter ps) {
        TTable ttable = vtable.getTTable();
        //static get
        generateMapGetBy(ttable.getPrimaryKey(), name, ps, true);

        //static getByXxx
        for (Map<String, Type> uniqueKey : ttable.getUniqueKeys()) {
            generateMapGetBy(uniqueKey, name, ps, false);
        }

        //static all
        ps.println1("public static java.util.Collection<" + name.className + "> all() {");
        ps.println2("%s.ConfigMgr mgr = %s.ConfigMgr.getMgr();", Name.codeTopPkg, Name.codeTopPkg);
        ps.println2("return mgr.%sAll.values();", name.containerPrefix);
        ps.println1("}");
        ps.println();

        //static _createAll
        ps.println1("public static void _createAll(%s.ConfigMgr mgr, configgen.genjava.ConfigInput input) {", Name.codeTopPkg);
        ps.println2("for (int c = input.readInt(); c > 0; c--) {");
        ps.println3("%s self = %s._create(input);", name.className, name.className);
        generateAllMapPut(ttable, name, ps);
        ps.println2("}");
        ps.println1("}");
        ps.println();

        //static _resolveAll
        if (tbean.hasRef()) {
            ps.println1("public static void _resolveAll(%s.ConfigMgr mgr) {", Name.codeTopPkg);
            ps.println2("for (%s e : mgr.%sAll.values()) {", name.className, name.containerPrefix);
            ps.println3("e._resolve(mgr);");
            ps.println2("}");
            ps.println1("}");
            ps.println();
        }
    }


    private static void generateMapGetBy(Map<String, Type> keys, BeanName name, CachedIndentPrinter ps, boolean isPrimaryKey) {
        if (keys.size() > 1) {
            generateKeyClass(keys, ps);
        }

        String mapName = name.containerPrefix + (isPrimaryKey ? "All" : Name.uniqueKeyMapName(keys));
        String keyName = Name.keyClassName(keys);
        if (keys.size() > 1) {
            keyName = name.fullName + "." + keyName;
        }

        mapsInMgr.add(String.format("    public final java.util.Map<%s, %s> %s = new java.util.LinkedHashMap<>();", keyName, name.fullName, mapName));
        //mgrPrint.println1("public final java.util.Map<%s, %s> %s = new java.util.LinkedHashMap<>();", keyName, name.fullName, mapName);

        String getByName = isPrimaryKey ? "get" : Name.uniqueKeyGetByName(keys);
        ps.println1("public static " + name.className + " " + getByName + "(" + MethodStr.formalParams(keys) + ") {");
        ps.println2("%s.ConfigMgr mgr = %s.ConfigMgr.getMgr();", Name.codeTopPkg, Name.codeTopPkg);
        ps.println2("return mgr." + mapName + ".get(" + MethodStr.actualParamsKey(keys, "") + ");");
        ps.println1("}");
        ps.println();
    }

    private static void generateAllMapPut(TTable ttable, BeanName name, CachedIndentPrinter ps) {
        generateMapPut(ttable.getPrimaryKey(), name, ps, true);
        for (Map<String, Type> uniqueKey : ttable.getUniqueKeys()) {
            generateMapPut(uniqueKey, name, ps, false);
        }
    }

    private static void generateMapPut(Map<String, Type> keys, BeanName name, CachedIndentPrinter ps, boolean isPrimaryKey) {
        String mapName = name.containerPrefix + (isPrimaryKey ? "All" : Name.uniqueKeyMapName(keys));
        ps.println3("mgr." + mapName + ".put(" + MethodStr.actualParamsKey(keys, "self.") + ", self);");
    }

    private static void generateKeyClass(Map<String, Type> keys, CachedIndentPrinter ps) {
        String keyClassName = Name.keyClassName(keys);
        //static Key class
        ps.println1("public static class " + keyClassName + " {");
        keys.forEach((n, t) -> ps.println2("private " + TypeStr.type(t) + " " + Generator.lower1(n) + ";"));
        ps.println();

        ps.println2(keyClassName + "(" + MethodStr.formalParams(keys) + ") {");
        keys.forEach((n, t) -> ps.println3("this." + Generator.lower1(n) + " = " + Generator.lower1(n) + ";"));
        ps.println2("}");
        ps.println();

        ps.println2("@Override");
        ps.println2("public int hashCode() {");
        ps.println3("return " + MethodStr.hashCodes(keys) + ";");
        ps.println2("}");
        ps.println();

        ps.println2("@Override");
        ps.println2("public boolean equals(Object other) {");
        ps.println3("if (null == other || !(other instanceof " + keyClassName + "))");
        ps.println4("return false;");
        ps.println3(keyClassName + " o = (" + keyClassName + ") other;");
        ps.println3("return " + MethodStr.equals(keys) + ";");
        ps.println2("}");

        ps.println1("}");
        ps.println();
    }
}
