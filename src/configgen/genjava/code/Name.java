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
            }catch(Exception e){
                System.out.println(e);
                return null;

            }

    }

    static String multiKeyClassName(String[] keys) {
        return Stream.of(keys).map(Generator::upper1).reduce("", (a, b) -> a + b) + "Key";
    }

    private static String fullName(TBean tbean, TForeignKey tfk) {
        String name = fullName(tfk.refTable);
        if (tfk.foreignKeyDefine.keys.length == 1) {
            String k = tfk.foreignKeyDefine.keys[0];
            Type tt = tbean.getColumnMap().get(k);
            if (tt instanceof TList) {
                return "java.util.List<" + name + ">";
            } else if (tt instanceof TMap) {
                //TODO
                return "";
            }
        }
        return name;
    }

    static String fullName(TBean tbean) {
        return new BeanName(tbean).fullName;
    }

    static String fullName(TTable ttable) {
        return fullName(ttable.getTBean());
    }

    static String tableDataFullName(TTable ttable) {
        String fn = fullName(ttable.getTBean());
        if (ttable.getTableDefine().isEnumFull() && !ttable.getTableDefine().isEnumHasOnlyPrimaryKeyAndEnumStr()) {
            fn = fn + "_Detail";
        }
        return fn;
    }

    static String listRefFullName(TBean tbean, TForeignKey tfk) {
        return "java.util.List<" + fullName(tbean, tfk) + ">";
    }

    static String refType(Type t, SRef ref) {
        if (t instanceof TList) {
            return "java.util.List<" + fullName(ref.refTable) + ">";
        } else if (t instanceof TMap) {
            return "java.util.Map<"
                    + (ref.mapKeyRefTable != null ? fullName(ref.mapKeyRefTable) : TypeStr.boxType(((TMap) t).key)) + ", "
                    + (ref.refTable != null ? fullName(ref.refTable) : TypeStr.boxType(((TMap) t).value)) + ">";
        } else {
            return fullName(ref.refTable);
        }
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

    static String refInitialValue(Type t) {
        if (t instanceof TList) {
            return " = new java.util.ArrayList<>()";
        } else if (t instanceof TMap) {
            return " = new java.util.LinkedHashMap<>();";
        } else {
            return "";
        }
    }
}
