package configgen.genjava.code;

import configgen.type.*;

class TypeStr {

    static String type(Type t) {
        return _type(t, false);
    }

    static String boxType(Type t) {
        return _type(t, true);
    }

    private static String _type(Type t, boolean box) {
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return box ? "Boolean" : "boolean";
            }

            @Override
            public String visit(TInt type) {
                return box ? "Integer" : "int";
            }

            @Override
            public String visit(TLong type) {
                return box ? "Long" : "long";
            }

            @Override
            public String visit(TFloat type) {
                return box ? "Float" : "float";
            }

            @Override
            public String visit(TString type) {
                return "String";
            }

            @Override
            public String visit(TList type) {
                return "java.util.List<" + _type(type.value, true) + ">";
            }

            @Override
            public String visit(TMap type) {
                return "java.util.Map<" + _type(type.key, true) + ", " + _type(type.value, true) + ">";
            }

            @Override
            public String visit(TBean type) {
                return Name.fullName(type);
            }

            @Override
            public String visit(TBeanRef type) {
                return Name.fullName(type.tBean);
            }
        });
    }

    static String initialValue(Type t) {
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return "";
            }

            @Override
            public String visit(TInt type) {
                return "";
            }

            @Override
            public String visit(TLong type) {
                return "";
            }

            @Override
            public String visit(TFloat type) {
                return "";
            }

            @Override
            public String visit(TString type) {
                return "";
            }

            @Override
            public String visit(TList type) {
                return " = new java.util.ArrayList<>()";
            }

            @Override
            public String visit(TMap type) {
                return " = new java.util.LinkedHashMap<>()";
            }

            @Override
            public String visit(TBean type) {
                return "";
            }

            @Override
            public String visit(TBeanRef type) {
                return "";
            }
        });
    }

    static String readValue(Type t) {
        return t.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return "input.readBool()";
            }

            @Override
            public String visit(TInt type) {
                return "input.readInt()";
            }

            @Override
            public String visit(TLong type) {
                return "input.readLong()";
            }

            @Override
            public String visit(TFloat type) {
                return "input.readFloat()";
            }

            @Override
            public String visit(TString type) {
                return "input.readStr()";
            }

            @Override
            public String visit(TList type) {
                return null;
            }

            @Override
            public String visit(TMap type) {
                return null;
            }

            @Override
            public String visit(TBean type) {
                return Name.fullName(type) + "._create(input)";
            }

            @Override
            public String visit(TBeanRef type) {
                return Name.fullName(type.tBean) + "._create(input)";
            }
        });

    }
}
