package configgen.define;

import configgen.view.DefineView;

import java.util.Arrays;

public class Ref {
    public final String table;
    public String[] cols;

    Ref(String self) {
        if (!self.isEmpty()) {
            String[] r = self.split(",");
            table = r[0];
            cols = Arrays.copyOfRange(r, 1, r.length);
        } else {
            table = "";
            cols = new String[0];
        }
    }

    public boolean refToPrimaryKey() {
        return cols.length == 0;
    }

    public void autoFixDefine(Bean parentBean, AllDefine defineToFix) {
        String fullRefTableName = resolveTableFullName(parentBean, table);
        Table refTable = defineToFix.getTable(fullRefTableName);
        if (refTable == null) {
            throw new AssertionError(String.format("[%s]要求索引到表=[%s]，不存在", parentBean.fullName(), table));
        }
        if (Arrays.equals(refTable.primaryKey, cols)) {
            System.out.printf("[%s]索引到表=[%s]的主键=\"%s\"，默认就到主键，只配表就行了，不用明确配主键字段\n",
                              parentBean.fullName(), table, String.join(",", cols));
            cols = new String[0]; // 只要是索引到table主键，就不要填cols， 所以也用cols.length == 0来判断是否主键。
        }
    }

    @Override
    public String toString() {
        if (cols.length == 0)
            return table;

        return table + "," + String.join(",", cols);
    }

    boolean valid(Bean parent, DefineView defineView) {
        String resolvedTable = resolveTableFullName(parent, table);
        Table t = defineView.tables.get(resolvedTable);
        return t != null && t.bean.columns.keySet().containsAll(Arrays.asList(cols));
    }

    public static String resolveBeanFullName(Bean parentBean, String refName) {
        if (refName.contains(".")) {
            return refName.startsWith(".") ? refName.substring(1) : refName;
        }

        String pkgName = parentBean.getPkgName();
        if (pkgName.isEmpty()) {
            return refName;
        }

        String fullName = pkgName + "." + refName;
        if (parentBean.getDefine().allDefine.getBean(fullName) != null) {
            return fullName;
        }

        return refName;
    }

    public static String resolveTableFullName(Bean parentBean, String refName) {
        if (refName.contains(".")) {
            return refName.startsWith(".") ? refName.substring(1) : refName;
        }

        String pkgName = parentBean.getPkgName();
        if (pkgName.isEmpty()) {
            return refName;
        }

        String fullName = pkgName + "." + refName;
        if (parentBean.getDefine().allDefine.getTable(fullName) != null) {
            return fullName;
        }

        return refName;
    }


}
