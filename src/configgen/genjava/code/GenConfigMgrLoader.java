package configgen.genjava.code;

import configgen.util.CachedIndentPrinter;
import configgen.value.AllValue;
import configgen.value.VTable;

class GenConfigMgrLoader {

    static void generate(AllValue vdb, CachedIndentPrinter ps) {
        ps.println("package %s;", Name.codeTopPkg);
        ps.println();

        ps.println("public class ConfigMgrLoader {");
        ps.println();

        ps.println1("public static ConfigMgr load(configgen.genjava.ConfigInput input) {");
        ps.println2("ConfigMgr mgr = new ConfigMgr();");

        int cnt = 0;
        for (VTable vTable : vdb.getVTables()) {
            if (vTable.getTTable().getTableDefine().isEnumFull() && vTable.getTTable().getTableDefine().isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                continue;
            }
            cnt++;
        }

        ps.println2("int c = input.readInt();");
        ps.println2("if (c < %d) {", cnt);
        ps.println3("throw new IllegalArgumentException();");
        ps.println2("}");
        ps.println2("for (int i = 0; i < c; i++) {");
        ps.println3("String tableName = input.readStr();");
        ps.println3("int tableSize = input.readInt();");
        ps.println3("switch (tableName) {");
        for (VTable vTable : vdb.getVTables()) {
            if (vTable.getTTable().getTableDefine().isEnumFull() && vTable.getTTable().getTableDefine().isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                continue;
            }

            ps.println4("case \"%s\":", vTable.name);
            ps.println5("%s._createAll(mgr, input);", Name.tableDataFullName(vTable.getTTable()));
            ps.println5("break;");
        }

        ps.println4("default:");
        ps.println5("input.skipBytes(tableSize);");
        ps.println5("break;");
        ps.println3("}");
        ps.println2("}");
        ps.println();

        ps.println2("_resolveAll(mgr);");

        ps.println2("return mgr;");
        ps.println1("}");

        ps.println1("private static void _resolveAll(ConfigMgr mgr) {");
        for (VTable vTable : vdb.getVTables()) {
            if (vTable.getTTable().getTableDefine().isEnumFull() && vTable.getTTable().getTableDefine().isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                continue;
            }

            if (vTable.getTTable().getTBean().hasRef()) {
                ps.println2("%s._resolveAll(mgr);", Name.tableDataFullName(vTable.getTTable()));
            }
        }
        ps.println1("}");
        ps.println("}");
    }

}
