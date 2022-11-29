package configgen.genjava.code;

import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.ForeignKey;
import configgen.define.Table;
import configgen.gen.Generator;
import configgen.type.*;
import configgen.util.CachedIndentPrinter;
import configgen.value.VTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class GenBeanClass {

    static void generateBuilder(TBean tbean, BeanName name, CachedIndentPrinter ps) {

        ps.println("package %s;", name.pkg);
        ps.println();

        ps.println("public class %sBuilder {", name.className);


        // field, public，并且都不设置默认值，如果要做，可以自己来包装
        for (Type type : tbean.getColumns()) {
            ps.println1("public %s %s;",
                        TypeStr.type(type),
                        Generator.lower1(type.getColumnName()));
        }
        ps.println();

        // build
        ps.println1("public %s build() {", name.className);
        for (Type type : tbean.getColumns()) {
            if (!type.isPrimitiveValueType()) {
                ps.println2("java.util.Objects.requireNonNull(%s);", Generator.lower1(type.getColumnName()));
            }
        }


        ps.println2("return new %s(this);", name.className);
        ps.println1("}");
        ps.println();

        ps.println("}");
    }

    static void generate(TBean tbean, VTable vtable, BeanName name, CachedIndentPrinter ps, boolean isTableUgc) {
        boolean isBean = vtable == null;
        boolean isTable = !isBean;

        boolean isBeanAndHasNoColumn = isBean && tbean.getColumns().isEmpty();
        boolean isChildDynamicBean = tbean.getBeanDefine().type == Bean.BeanType.ChildDynamicBean;

        ps.println("package " + name.pkg + ";");
        ps.println();
        if (isChildDynamicBean) {
            TBean baseAction = (TBean) tbean.parent;
            ps.println("public class " + name.className + " implements " + Name.fullName(baseAction) + " {");
            ps.println1("@Override");
            ps.println1("public " + Name.refType(baseAction.getChildDynamicBeanEnumRefTable()) + " type() {");
            ps.println2("return " + Name.refType(
                    baseAction.getChildDynamicBeanEnumRefTable()) + "." + tbean.name.toUpperCase() + ";");
            ps.println1("}");
            ps.println();
        } else {
            ps.println("public class " + name.className + " {");
        }

        //field
        for (Type type : tbean.getColumns()) {
            ps.println1("private %s %s;",
                        TypeStr.type(type),
                        Generator.lower1(type.getColumnName()));
            for (SRef r : type.getConstraint().references) {
                ps.println1("private %s %s;",
                            Name.refType(type, r),
                            Name.refName(r));
            }
        }

        for (TForeignKey foreignKey : tbean.getMRefs()) {
            ps.println1("private %s %s;",
                        Name.refType(foreignKey),
                        Name.refName(foreignKey));
        }
        for (TForeignKey foreignKey : tbean.getListRefs()) {
            ps.println1("private %s %s;",
                        Name.refTypeForList(foreignKey),
                        Name.refName(foreignKey));
        }
        ps.println();

        //constructor
        //noinspection StatementWithEmptyBody
        if (isBeanAndHasNoColumn) {
            // 如果是没有列的bean
            // 后面会生成空参数的public构造函数
            // 这里忽略
        } else {
            ps.println1("private %s() {", name.className);
            ps.println1("}");
            ps.println();
        }

        //bean有构造器，可能会ugc的table由builder生成，这里提供package访问级别构造器
        if (isBean) {
            ps.println1("public %s(%s) {", name.className, MethodStr.formalParams(tbean.getColumnMap()));
            for (String n : tbean.getColumnMap().keySet()) {
                String ln = Generator.lower1(n);
                ps.println2("this.%s = %s;", ln, ln);
            }
            ps.println1("}");
            ps.println();
        } else if (isTableUgc) {
            // package访问级别，只能通过builder.build()来构建
            ps.println1("%s(%sBuilder b) {", name.className, name.className);
            for (String n : tbean.getColumnMap().keySet()) {
                String ln = Generator.lower1(n);
                ps.println2("this.%s = b.%s;", ln, ln);
            }
            ps.println1("}");
            ps.println();
        }

        //static create from ConfigInput
        ps.println1("public static %s _create(configgen.genjava.ConfigInput input) {", name.className);
        ps.println2("%s self = new %s();", name.className, name.className);

        for (Map.Entry<String, Type> f : tbean.getColumnMap().entrySet()) {

            Type t = f.getValue();
            String ln = Generator.lower1(f.getKey());
            if (t instanceof TList) {
                ps.println2("self.%s = new java.util.ArrayList<>();", ln);
                ps.println2("for (int c = input.readInt(); c > 0; c--) {");
                ps.println3("self.%s.add(%s);", ln, TypeStr.readValue(((TList) t).value));
                ps.println2("}");
            } else if (t instanceof TMap) {
                ps.println2("self.%s = new java.util.LinkedHashMap<>();", ln);
                ps.println2("for (int c = input.readInt(); c > 0; c--) {");
                ps.println3("self.%s.put(%s, %s);", ln, TypeStr.readValue(((TMap) t).key),
                            TypeStr.readValue(((TMap) t).value));
                ps.println2("}");
            } else {
                ps.println2("self.%s = %s;", ln, TypeStr.readValue(t));
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
            ps.println1("public %s %s() {",
                        Name.refType(tForeignKey),
                        Generator.lower1(Name.refName(tForeignKey)));
            ps.println2("return %s;",
                        Name.refName(tForeignKey));
            ps.println1("}");
            ps.println();
        }

        for (TForeignKey tForeignKey : tbean.getListRefs()) {
            ps.println1("public %s %s() {",
                        Name.refTypeForList(tForeignKey),
                        Generator.lower1(Name.refName(tForeignKey)));
            ps.println2("return %s;",
                        Name.refName(tForeignKey));
            ps.println1("}");
            ps.println();
        }

        if (isBeanAndHasNoColumn) {
            ps.println1("@Override");
            ps.println1("public int hashCode() {");
            ps.println2("return " + name.className + ".class.hashCode();");
            ps.println1("}");
            ps.println();

            ps.println1("@Override");
            ps.println1("public boolean equals(Object other) {");
            ps.println2("return this == other || other instanceof " + name.className + ";");
            ps.println1("}");
            ps.println();


        } else if (isBean) {
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
        String beanName = "";
        if (isChildDynamicBean)
            beanName = name.className;
        ps.println1("@Override");
        ps.println1("public String toString() {");
        if (isBeanAndHasNoColumn) {
            ps.println2("return \"%s\";", beanName);
        } else {
            String params = tbean.getColumnMap().keySet().stream().map(Generator::lower1).collect(
                    Collectors.joining(" + \",\" + "));
            ps.println2("return \"%s(\" + %s + \")\";", beanName, params);
        }
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
            String ln = Generator.lower1(f.getKey());
            Type t = f.getValue();
            if (t.hasRef()) {
                if (t instanceof TList) {
                    TList tt = (TList) t;

                    for (SRef sr : t.getConstraint().references) {
                        ps.println2("%s = new java.util.ArrayList<>();", Name.refName(sr));
                    }

                    ps.println2("%s.forEach( e -> {", ln);
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

                    for (SRef sr : t.getConstraint().references) {
                        ps.println2("%s = new java.util.LinkedHashMap<>();", Name.refName(sr));
                    }

                    ps.println2("%s.forEach( (k, v) -> {", ln);
                    if (tt.key instanceof TBeanRef && tt.key.hasRef()) {
                        ps.println3("k._resolve(mgr);");
                    }
                    if (tt.value instanceof TBeanRef && tt.value.hasRef()) {
                        ps.println3("v._resolve(mgr);");
                    }

                    for (SRef sr : t.getConstraint().references) {
                        String k = "k";
                        if (sr.mapKeyRefTable != null) {
                            ps.println3(Name.refTypeForMapKey(sr) + " rk = " + MethodStr.tableGet(sr.mapKeyRefTable,
                                                                                                  sr.mapKeyRefCols,
                                                                                                  "k"));
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
                        ps.println2("%s._resolve(mgr);", ln);
                    }
                    for (SRef sr : t.getConstraint().references) {
                        ps.println2(Name.refName(sr) + " = " + MethodStr.tableGet(sr.refTable, sr.refCols, ln));
                        if (!sr.refNullable)
                            ps.println2("java.util.Objects.requireNonNull(" + Name.refName(sr) + ");");
                    }
                }
            }
        } //end columns

        for (TForeignKey m : tbean.getMRefs()) {
            ps.println2(Name.refName(m) + " = " + MethodStr.tableGet(m.refTable, m.foreignKeyDefine.ref.cols,
                                                                     MethodStr.actualParams(m.foreignKeyDefine.keys)));
            if (m.foreignKeyDefine.refType != ForeignKey.RefType.NULLABLE)
                ps.println2("java.util.Objects.requireNonNull(" + Name.refName(m) + ");");
        }

        for (TForeignKey l : tbean.getListRefs()) {
            ps.println2("%s = new java.util.ArrayList<>();", Name.refName(l));

            BeanName refn = new BeanName(l.refTable.getTBean());
            Table refTableDefine = l.refTable.getTableDefine();
            boolean has_OnlyEnum = refTableDefine.isEnumFull() && refTableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr();
            boolean has_Enum_Detail = refTableDefine.isEnumFull() && !refTableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr();
            if (has_OnlyEnum) {
                ps.println2("for (%s v : %s.values()) {", refn.fullName, refn.fullName);
            } else if (has_Enum_Detail) {
                ps.println2("for (%s vv : %s.values()) {", refn.fullName, refn.fullName);
                String primK = l.refTable.getTableDefine().primaryKey[0];
                ps.println3("%s v = mgr.%sAll.get(vv.get%s());", refn.fullName + "_Detail", refn.containerPrefix,
                            Generator.upper1(primK));
            } else {
                ps.println2("mgr.%sAll.values().forEach( v -> {", refn.containerPrefix); // 为了跟之前兼容
            }

            List<String> eqs = new ArrayList<>();
            for (int i = 0; i < l.foreignKeyDefine.keys.length; i++) {
                String k = l.foreignKeyDefine.keys[i];
                String rk = l.foreignKeyDefine.ref.cols[i];
                eqs.add(MethodStr.equal("v.get" + Generator.upper1(rk) + "()", Generator.lower1(k),
                                        tbean.getColumnMap().get(k)));
            }
            ps.println3("if (" + String.join(" && ", eqs) + ")");

            if (has_OnlyEnum) {
                ps.println4(Name.refName(l) + ".add(v);");
                ps.println2("}");
            } else if (has_Enum_Detail) {
                ps.println4(Name.refName(l) + ".add(vv);");
                ps.println2("}");
            } else {
                ps.println4(Name.refName(l) + ".add(v);");
                ps.println2("});");
            }
        }

        ps.println1("}");
        ps.println();
    }

}
