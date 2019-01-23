package configgen.genlua;

import configgen.define.Bean;
import configgen.gen.Generator;
import configgen.type.*;

import java.util.Map;
import java.util.stream.Stream;

public class Name {

    private static String pkgPrefix = "";

    static void setPackageName(String pkgName) {
        if (pkgName.length() == 0) {
            pkgPrefix = pkgName;
        } else {
            pkgPrefix = pkgName + ".";
        }
    }

    static String uniqueKeyGetByName(Map<String, Type> keys) {
        return "getBy" + keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b);
    }

    static String uniqueKeyMapName(Map<String, Type> keys) {
        return keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b) + "Map";
    }

    static String uniqueKeyGetByName(String[] cols) {
        if (cols.length == 0) //ref to primary key
            return "get";
        else
            return "getBy" + Stream.of(cols).map(Generator::upper1).reduce("", (a, b) -> a + b);
    }

    static String refName(SRef sr) {
        return (sr.refNullable ? "NullableRef" : "Ref") + Generator.upper1(sr.name);
    }

    static String refName(TForeignKey fk) {
        switch (fk.foreignKeyDefine.refType) {
            case NORMAL:
                return "Ref" + Generator.upper1(fk.name);
            case NULLABLE:
                return "NullableRef" + Generator.upper1(fk.name);
            default:
                return "ListRef" + Generator.upper1(fk.name);
        }
    }

    static String fullName(TBean tbean) {
        if (tbean.getBeanDefine().type == Bean.BeanType.Table) {
            return pkgPrefix + tbean.name.toLowerCase();
        } else if (tbean.getBeanDefine().type == Bean.BeanType.ChildDynamicBean) {
            return "Beans." + (((TBean) tbean.parent)).name.toLowerCase() + "." + tbean.name.toLowerCase();
        } else {
            return "Beans." + tbean.name.toLowerCase();
        }
    }

    static String fullName(TTable ttable) {
        return fullName(ttable.getTBean());
    }

    static String tablePath(String tableName) {
        return tableName.replace('.', '/').toLowerCase() + ".lua";
    }
}
