package configgen.genjava.code;

import configgen.define.Column;
import configgen.gen.Generator;
import configgen.type.SRef;
import configgen.type.TBean;
import configgen.type.TForeignKey;
import configgen.util.CachedIndentPrinter;
import configgen.value.VTable;

import java.util.Map;

class GenEnumClass {

    static void generate(VTable vtable, BeanName name, CachedIndentPrinter ps, boolean isFull, boolean isNeedReadData, BeanName dataName) {
        ps.println("package " + name.pkg + ";");
        ps.println();

        ps.println((isFull ? "public enum " : "public class ") + name.className + " {");

        boolean hasIntValue = !vtable.getTTable().getTableDefine().isEnumAsPrimaryKey();
        if (!hasIntValue) {
            int len = vtable.getEnumNames().size();
            int c = 0;
            for (String enumName : vtable.getEnumNames()) {
                c++;
                String fix = c == len ? ";" : ",";
                if (isFull) {
                    ps.println1("%s(\"%s\")%s", enumName.toUpperCase(), enumName, fix);
                } else {
                    ps.println1("public static %s %s = new %s(\"%s\");", name.className, enumName.toUpperCase(), name.className, enumName);
                }
            }

            ps.println();
            ps.println1("private String value;");
            ps.println();

            ps.println1("%s(String value) {", name.className);
            ps.println2("this.value = value;");
            ps.println1("}");
            ps.println();

//            ps.println1("public String getValue() {");
//            ps.println2("return value;");
//            ps.println1("}");
//            ps.println();

        } else {
            int len = vtable.getEnumName2IntegerValueMap().size();
            int c = 0;
            for (Map.Entry<String, Integer> entry : vtable.getEnumName2IntegerValueMap().entrySet()) {
                String enumName = entry.getKey();
                int value = entry.getValue();
                c++;
                String fix = c == len ? ";" : ",";

                if (isFull) {
                    ps.println1("%s(\"%s\", %d)%s", enumName.toUpperCase(), enumName, value, fix);
                } else {
                    ps.println1("public static %s %s = new %s(\"%s\", %d);", name.className, enumName.toUpperCase(), name.className, enumName, value);
                }
            }

            ps.println();
            ps.println1("private String name;");
            ps.println1("private int value;");

            ps.println();

            ps.println1("%s(String name, int value) {", name.className);
            ps.println2("this.name = name;");
            ps.println2("this.value = value;");
            ps.println1("}");
            ps.println();

//            ps.println1("public String getName() {");
//            ps.println2("return name;");
//            ps.println1("}");
//            ps.println();
//
//            ps.println1("public int getValue() {");
//            ps.println2("return value;");
//            ps.println1("}");
//            ps.println();
        }


        if (isFull) {
            ps.println1("private static java.util.Map<%s, %s> map = new java.util.HashMap<>();", hasIntValue ? "Integer" : "String", name.className);
            ps.println();

            ps.println1("static {");
            ps.println2("for(%s e : %s.values()) {", name.className, name.className);
            ps.println3("map.put(e.value, e);");
            ps.println2("}");
            ps.println1("}");
            ps.println();

            ps.println1("public static %s get(%s value) {", name.className, hasIntValue ? "int" : "String");
            ps.println2("return map.get(value);");
            ps.println1("}");
            ps.println();

            TBean tbean = vtable.getTTable().getTBean();
            tbean.getColumnMap().forEach((n, t) -> {
                Column f = tbean.getBeanDefine().columns.get(n);
                if (!f.desc.isEmpty()) {
                    ps.println1("/**");
                    ps.println1(" * " + f.desc);
                    ps.println1(" */");
                }
                ps.println1("public " + TypeStr.type(t) + " get" + Generator.upper1(n) + "() {");
                if (f.name.equals(vtable.getTTable().getTableDefine().primaryKey[0])) {
                    ps.println2("return value;");
                } else if (f.name.equals(vtable.getTTable().getTableDefine().enumStr)) {
                    ps.println2("return name;");
                } else {
                    ps.println2("return ref().get" + Generator.upper1(n) + "();");
                }
                ps.println1("}");
                ps.println();

                for (SRef r : t.getConstraint().references) {
                    ps.println1("public " + Name.refType(t, r) + " " + Generator.lower1(Name.refName(r)) + "() {");
                    ps.println2("return ref()." + Generator.lower1(Name.refName(r)) + "();");
                    ps.println1("}");
                    ps.println();
                }
            });

            for (TForeignKey m : tbean.getMRefs()) {
                ps.println1("public " + Name.refType(m) + " " + Generator.lower1(Name.refName(m)) + "() {");
                ps.println2("return ref()." + Generator.lower1(Name.refName(m)) + "();");
                ps.println1("}");
                ps.println();
            }

            for (TForeignKey l : tbean.getListRefs()) {
                ps.println1("public " + Name.refTypeForList(l) + " " + Generator.lower1(Name.refName(l)) + "() {");
                ps.println2("return ref()." + Generator.lower1(Name.refName(l)) + "();");
                ps.println1("}");
                ps.println();
            }
        }

        if (isNeedReadData) {
            ps.println1("public %s ref() {", dataName.fullName);
            ps.println2("return %s.get(value);", dataName.fullName);
            ps.println1("}");
            ps.println();
        }

        ps.println("}");
    }
}
