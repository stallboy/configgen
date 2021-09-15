package configgen.define;

import configgen.view.DefineView;

import java.util.Arrays;

public class Ref {
    public final String table;
    public final String[] cols;

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

    public static String resolveBeanFullName(Bean parentBean , String refName) {
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
