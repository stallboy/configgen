package configgen.genjava.code;

import configgen.gen.Generator;
import configgen.type.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

class MethodStr {

    static String formalParams(Map<String, Type> fs) {
        return fs.entrySet().stream().map(e -> TypeStr.type(e.getValue()) + " " + Generator.lower1(e.getKey())).collect(Collectors.joining(", "));
    }

    static String actualParams(String[] keys) {
        return Arrays.stream(keys).map(Generator::lower1).collect(Collectors.joining(", "));
    }

    static String actualParamsKey(Map<String, Type> keys, String pre) {
        String p = keys.entrySet().stream().map(e -> pre + Generator.lower1(e.getKey())).collect(Collectors.joining(", "));
        return keys.size() > 1 ? "new " + Name.keyClassName(keys) + "(" + p + ")" : p;
    }

    static String hashCodes(Map<String, Type> fs) {
        return fs.entrySet().stream().map(e -> hashCode(e.getKey(), e.getValue())).collect(Collectors.joining(" + "));
    }

    private static String hashCode(String name, Type t) {
        String n = Generator.lower1(name);
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return "Boolean.hashCode(" + n + ")";
            }

            @Override
            public String visit(TInt type) {
                return n;
            }

            @Override
            public String visit(TLong type) {
                return "Long.hashCode(" + n + ")";
            }

            @Override
            public String visit(TFloat type) {
                return "Float.hashCode(" + n + ")";
            }

            @Override
            public String visit(TString type) {
                return n + ".hashCode()";
            }

            @Override
            public String visit(TList type) {
                return n + ".hashCode()";
            }

            @Override
            public String visit(TMap type) {
                return n + ".hashCode()";
            }

            @Override
            public String visit(TBean type) {
                return n + ".hashCode()";
            }

            @Override
            public String visit(TBeanRef type) {
                return n + ".hashCode()";
            }
        });
    }

    static String equals(Map<String, Type> fs) {
        return fs.entrySet().stream().map(e -> equal(Generator.lower1(e.getKey()), "o." + Generator.lower1(e.getKey()), e.getValue())).collect(Collectors.joining(" && "));
    }

    static String equal(String a, String b, Type t) {
        boolean eq = t.accept(new TypeVisitorT<Boolean>() {
            @Override
            public Boolean visit(TBool type) {
                return false;
            }

            @Override
            public Boolean visit(TInt type) {
                return false;
            }

            @Override
            public Boolean visit(TLong type) {
                return false;
            }

            @Override
            public Boolean visit(TFloat type) {
                return false;
            }

            @Override
            public Boolean visit(TString type) {
                return true;
            }

            @Override
            public Boolean visit(TList type) {
                return true;
            }

            @Override
            public Boolean visit(TMap type) {
                return true;
            }

            @Override
            public Boolean visit(TBean type) {
                return true;
            }

            @Override
            public Boolean visit(TBeanRef type) {
                return true;
            }
        });
        return eq ? a + ".equals(" + b + ")" : a + " == " + b;
    }

    static String tableGet(TTable ttable, String[] cols, String actualParam) {
        boolean isPrimaryKey = cols.length == 0;
        BeanName name = new BeanName(ttable.getTBean());

        if (ttable.getTableDefine().isEnumFull()) {
            return name.fullName + ".get(" + actualParam + ");";
        } else {
            String pre = "mgr." + name.containerPrefix;

            if (isPrimaryKey) {//ref to primary key
                if (ttable.getPrimaryKey().size() == 1) {
                    return pre + "All.get(" + actualParam + ");";
                } else {
                    return pre + "All.get(new " + name.fullName + "." + Name.multiKeyClassName(ttable.getTableDefine().primaryKey) + "(" + actualParam + ") );";
                }
            } else {
                if (cols.length == 1) {
                    return pre + Name.uniqueKeyMapName(cols) + ".get(" + actualParam + ");";
                } else {
                    return pre + Name.uniqueKeyMapName(cols) + ".get( new " + name.fullName + "." + Name.multiKeyClassName(cols) + "(" + actualParam + ") );";
                }
            }
        }
    }

}
