package configgen.genjava.code;

import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.ForeignKey;
import configgen.gen.Generator;
import configgen.type.*;
import configgen.util.CachedIndentPrinter;
import configgen.value.VTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class GenBeanClass {

    static void generate(TBean tbean, VTable vtable, BeanName name, CachedIndentPrinter ps) {
        boolean isBean = vtable == null;
        boolean isTable = !isBean;

        ps.println("package " + name.pkg + ";");
        ps.println();
        if (tbean.getBeanDefine().type == Bean.BeanType.ChildDynamicBean) {
            TBean baseAction = (TBean) tbean.parent;
            ps.println("public class " + name.className + " implements " + Name.fullName(baseAction) + " {");
            ps.println1("@Override");
            ps.println1("public " + Name.refType(baseAction.getChildDynamicBeanEnumRefTable()) + " type() {");
            ps.println2("return " + Name.refType(baseAction.getChildDynamicBeanEnumRefTable()) + "." + tbean.name.toUpperCase() + ";");
            ps.println1("}");
            ps.println();
        } else {
            ps.println("public class " + name.className + " {");
        }

        //field
        for (Type type : tbean.getColumns()) {
            ps.println1("private " + TypeStr.type(type) + " " + Generator.lower1(type.getColumnName()) + TypeStr.initialValue(type) + ";");
            for (SRef r : type.getConstraint().references) {
                ps.println1("private " + Name.refType(type, r) + " " + Name.refName(r) + Name.refInitialValue(type) + ";");
            }
        }

        for (TForeignKey foreignKey : tbean.getMRefs()) {
            ps.println1("private " + Name.refType(foreignKey) + " " + Name.refName(foreignKey) + ";");
        }
        for (TForeignKey foreignKey : tbean.getListRefs()) {
            ps.println1("private " + Name.refTypeForList(foreignKey) + " " + Name.refName(foreignKey) + " = new java.util.ArrayList<>();");
        }
        ps.println();

        //constructor
        ps.println1("private %s() {", name.className);
        ps.println1("}");
        ps.println();

        if (isBean) {
            ps.println1("public %s(%s) {", name.className, MethodStr.formalParams(tbean.getColumnMap()));
            tbean.getColumnMap().forEach((n, t) -> ps.println2("this.%s = %s;", Generator.lower1(n), Generator.lower1(n)));
            ps.println1("}");
            ps.println();
        }

        //static create from ConfigInput
        ps.println1("public static %s _create(configgen.genjava.ConfigInput input) {", name.className);
        ps.println2("%s self = new %s();", name.className, name.className);

        for (Map.Entry<String, Type> f : tbean.getColumnMap().entrySet()) {
            String n = f.getKey();
            Type t = f.getValue();
            String selfN = "self." + Generator.lower1(n);
            if (t instanceof TList) {
                ps.println2("for (int c = input.readInt(); c > 0; c--) {");
                ps.println3("%s.add(%s);", selfN, TypeStr.readValue(((TList) t).value));
                ps.println2("}");
            } else if (t instanceof TMap) {
                ps.println2("for (int c = input.readInt(); c > 0; c--) {");
                ps.println3("%s.put(%s, %s);", selfN, TypeStr.readValue(((TMap) t).key), TypeStr.readValue(((TMap) t).value));
                ps.println2("}");
            } else {
                ps.println2("%s = %s;", selfN, TypeStr.readValue(t));
            }
        }
        ps.println2("return self;");
        ps.println1("}");
        ps.println();


        //getter
        for (Map.Entry<String, Type> entry : tbean.getColumnMap().entrySet()) {
            String n = entry.getKey();
            Type t = entry.getValue();
            Column f = tbean.getBeanDefine().columns.get(n);
            if (!f.desc.isEmpty()) {
                ps.println1("/**");
                ps.println1(" * " + f.desc);
                ps.println1(" */");
            }

            ps.println1("public " + TypeStr.type(t) + " get" + Generator.upper1(n) + "() {");
            ps.println2("return " + Generator.lower1(n) + ";");
            ps.println1("}");
            ps.println();

            for (SRef r : t.getConstraint().references) {
                ps.println1("public " + Name.refType(t, r) + " " + Generator.lower1(Name.refName(r)) + "() {");
                ps.println2("return " + Name.refName(r) + ";");
                ps.println1("}");
                ps.println();
            }
        }

        for (TForeignKey tForeignKey : tbean.getMRefs()) {
            ps.println1("public " + Name.refType(tForeignKey) + " " + Generator.lower1(Name.refName(tForeignKey)) + "() {");
            ps.println2("return " + Name.refName(tForeignKey) + ";");
            ps.println1("}");
            ps.println();
        }

        for (TForeignKey tForeignKey : tbean.getListRefs()) {
            ps.println1("public " + Name.refTypeForList(tForeignKey) + " " + Generator.lower1(Name.refName(tForeignKey)) + "() {");
            ps.println2("return " + Name.refName(tForeignKey) + ";");
            ps.println1("}");
            ps.println();
        }

        //hashCode, equals
        if (isBean) {
            Map<String, Type> keys = tbean.getColumnMap();
            ps.println1("@Override");
            ps.println1("public int hashCode() {");
            ps.println2("return " + MethodStr.hashCodes(keys) + ";");
            ps.println1("}");
            ps.println();

            ps.println1("@Override");
            ps.println1("public boolean equals(Object other) {");
            ps.println2("if (!(other instanceof " + name.className + "))");
            ps.println3("return false;");
            ps.println2(name.className + " o = (" + name.className + ") other;");
            ps.println2("return " + MethodStr.equals(keys) + ";");
            ps.println1("}");
            ps.println();
        }

        //toString
        ps.println1("@Override");
        ps.println1("public String toString() {");
        ps.println2("return \"(\" + " + tbean.getColumnMap().keySet().stream().map(Generator::lower1).collect(Collectors.joining(" + \",\" + ")) + " + \")\";");
        ps.println1("}");
        ps.println();


        //_resolve
        if (tbean.hasRef()) {
            generateResolve(tbean, ps);
        }

        if (isTable) {
            GenBeanClassTablePart.generate(tbean, vtable, name, ps);
        }
        ps.println("}");
    }


    private static void generateResolve(TBean tbean, CachedIndentPrinter ps) {
        if (tbean.getBeanDefine().type == Bean.BeanType.ChildDynamicBean) {
            ps.println1("@Override");
        }
        ps.println1("public void _resolve(%s.ConfigMgr mgr) {", Name.codeTopPkg);

        for (Map.Entry<String, Type> f : tbean.getColumnMap().entrySet()) {
            String n = f.getKey();
            Type t = f.getValue();
            if (t.hasRef()) {
                if (t instanceof TList) {
                    TList tt = (TList) t;
                    ps.println2(Generator.lower1(n) + ".forEach( e -> {");
                    if (tt.value instanceof TBeanRef && tt.value.hasRef()) {
                        ps.println3("e._resolve(mgr);");
                    }
                    for (SRef sr : t.getConstraint().references) {
                        ps.println3(Name.refType(sr) + " r = " + MethodStr.tableGet(sr.refTable, sr.refCols, "e"));
                        ps.println3("java.util.Objects.requireNonNull(r);");
                        ps.println3(Name.refName(sr) + ".add(r);");
                    }
                    ps.println2("});");
                } else if (t instanceof TMap) {
                    TMap tt = (TMap) t;
                    ps.println2(Generator.lower1(n) + ".forEach( (k, v) -> {");
                    if (tt.key instanceof TBeanRef && tt.key.hasRef()) {
                        ps.println3("k._resolve(mgr);");
                    }
                    if (tt.value instanceof TBeanRef && tt.value.hasRef()) {
                        ps.println3("v._resolve(mgr);");
                    }
                    for (SRef sr : t.getConstraint().references) {
                        String k = "k";
                        if (sr.mapKeyRefTable != null) {
                            ps.println3(Name.refTypeForMapKey(sr) + " rk = " + MethodStr.tableGet(sr.mapKeyRefTable, sr.mapKeyRefCols, "k"));
                            ps.println3("java.util.Objects.requireNonNull(rk);");
                            k = "rk";
                        }
                        String v = "v";
                        if (sr.refTable != null) {
                            ps.println3(Name.refType(sr) + " rv = " + MethodStr.tableGet(sr.refTable, sr.refCols, "v"));
                            ps.println3("java.util.Objects.requireNonNull(rv);");
                            v = "rv";
                        }
                        ps.println3(Name.refName(sr) + ".put(" + k + ", " + v + ");");
                    }
                    ps.println2("});");
                } else {
                    if (t instanceof TBeanRef && t.hasRef()) {
                        ps.println2(Generator.lower1(n) + "._resolve(mgr);");
                    }
                    for (SRef sr : t.getConstraint().references) {
                        ps.println2(Name.refName(sr) + " = " + MethodStr.tableGet(sr.refTable, sr.refCols, Generator.lower1(n)));
                        if (!sr.refNullable)
                            ps.println2("java.util.Objects.requireNonNull(" + Name.refName(sr) + ");");
                    }
                }
            }
        } //end columns

        for (TForeignKey m : tbean.getMRefs()) {
            ps.println2(Name.refName(m) + " = " + MethodStr.tableGet(m.refTable, m.foreignKeyDefine.ref.cols, MethodStr.actualParams(m.foreignKeyDefine.keys)));
            if (m.foreignKeyDefine.refType != ForeignKey.RefType.NULLABLE)
                ps.println2("java.util.Objects.requireNonNull(" + Name.refName(m) + ");");
        }

        for (TForeignKey l : tbean.getListRefs()) {
            BeanName refn = new BeanName(l.refTable.getTBean());
            boolean isUseFor = l.refTable.getTableDefine().isEnumFull();
            if (isUseFor) { // 用于保证引用到_Detail类型的时引用解析生成正确
                ps.println2("for (%s v : %s.values()) {", refn.fullName, refn.fullName);
            } else {
                ps.println2("mgr.%sAll.values().forEach( v -> {", refn.containerPrefix);
            }

            List<String> eqs = new ArrayList<>();
            for (int i = 0; i < l.foreignKeyDefine.keys.length; i++) {
                String k = l.foreignKeyDefine.keys[i];
                String rk = l.foreignKeyDefine.ref.cols[i];
                eqs.add(MethodStr.equal("v.get" + Generator.upper1(rk) + "()", Generator.lower1(k), tbean.getColumnMap().get(k)));
            }
            ps.println3("if (" + String.join(" && ", eqs) + ")");
            ps.println4(Name.refName(l) + ".add(v);");
            if (isUseFor) {
                ps.println2("}");
            } else {
                ps.println2("});");
            }
        }

        ps.println1("}");
        ps.println();
    }

}
