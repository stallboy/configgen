package configgen.genjava.code;

import configgen.gen.Generator;
import configgen.type.*;

import java.util.Map;
import java.util.stream.Stream;

public class Name {

    static String codeTopPkg;

    static String uniqueKeyGetByName(Map<String, Type> keys) {
        return "getBy" + keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b);
    }

    static String uniqueKeyMapName(Map<String, Type> keys) {
        return keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b) + "Map";
    }

    static String uniqueKeyMapName(String[] keys) {
        return Stream.of(keys).map(Generator::upper1).reduce("", (a, b) -> a + b) + "Map";
    }

    static String keyClassName(Map<String, Type> keys) {
        if (keys.size() > 1)
            return keys.keySet().stream().map(Generator::upper1).reduce("", (a, b) -> a + b) + "Key";
        else
            try {
                return TypeStr.boxType(keys.values().iterator().next());
            } catch (Exception e) {
                return null;
            }

    }

    static String multiKeyClassName(String[] keys) {
        return Stream.of(keys).map(Generator::upper1).reduce("", (a, b) -> a + b) + "Key";
    }


    static String fullName(TBean tbean) {
        return new BeanName(tbean).fullName;
    }

    static String tableDataFullName(TTable ttable) {
        String fn = fullName(ttable.getTBean());
        if (ttable.getTableDefine().isEnumFull() && !ttable.getTableDefine().isEnumHasOnlyPrimaryKeyAndEnumStr()) {
            fn = fn + "_Detail";
        }
        return fn;
    }


    static String refType(TTable ttable) {
        return new BeanName(ttable.getTBean()).fullName;
    }


    static String refType(TForeignKey fk) {
        return refType(fk.refTable);
    }

    static String refType(SRef ref) {
        return refType(ref.refTable);
    }

    static String refType(Type t, SRef ref) {
        if (t instanceof TList) {
            return "java.util.List<" + refType(ref.refTable) + ">";
        } else if (t instanceof TMap) {
            return "java.util.Map<"
                    + (ref.mapKeyRefTable != null ? refTypeForMapKey(ref) : TypeStr.boxType(((TMap) t).key)) + ", "
                    + (ref.refTable != null ? refType(ref) : TypeStr.boxType(((TMap) t).value)) + ">";
        } else {
            return refType(ref);
        }
    }

    static String refTypeForMapKey(SRef ref) {
        return refType(ref.mapKeyRefTable);
    }

    static String refTypeForList(TForeignKey fk) {
        return "java.util.List<" + refType(fk.refTable) + ">";
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

}
