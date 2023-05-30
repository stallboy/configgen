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


    public void verifyDefine(Bean parentBean, String[] keys, boolean isAsMapKey, AllDefine fullDefine) { // type加上约束
        String fullRefTableName = resolveTableFullName(parentBean, table);
        Table refTable = fullDefine.getTable(fullRefTableName);
        if (refTable == null) {
            throw new AssertionError(String.format("[%s].[%s]要求索引到表=[%s]，此表不存在",
                    parentBean.fullName(), String.join(",", keys), table));
        }

        String[] toCols = cols;
        if (refToPrimaryKey()) {
            toCols = refTable.primaryKey;
        }

        if (keys.length != toCols.length) {
            throw new AssertionError(String.format("[%s].[%s]要求索引到表=[%s]，列的数量不一致",
                    parentBean.fullName(), String.join(",", keys), table));
        }

        for (String key : keys) {
            Column c = parentBean.getColumn(keys[0]);
            if (c == null) {
                throw new AssertionError(String.format("[%s].[%s] 设置外键，但这个列本身不存在",
                        parentBean.fullName(), key));
            }
        }

        if (isAsMapKey) {
            if (keys.length != 1) {
                throw new AssertionError(String.format("[%s].[%s]设置了keyRef，是个map，需要keys数量为1",
                        parentBean.fullName(), String.join(",", keys)));
            }
            Column selfCol = parentBean.getColumn(keys[0]);
            if (!selfCol.type.startsWith("map,")) {
                throw new AssertionError(String.format("[%s].[%s] 设置外键keyRef，应该是map，但不是",
                        parentBean.fullName(), keys[0]));
            }
        }

        for (int i = 0; i < keys.length; i++) {
            Column selfCol = parentBean.getColumn(keys[i]);
            Column toCol = refTable.bean.getColumn(toCols[i]);

            if (selfCol == null) {
                throw new AssertionError(String.format("[%s].[%s] 设置外键，但这个列本身不存在",
                        parentBean.fullName(), keys[i]));
            }
            if (toCol == null) {
                throw new AssertionError(String.format("[%s].[%s] 设置外键到[%s].[%s]，外键指向的表此列不存在",
                        parentBean.fullName(), keys[i], fullRefTableName, toCols[i]));
            }

            String selfType = selfCol.type;
            if (keys.length == 1) {
                if (selfType.startsWith("map,")) {
                    String[] sp = selfType.split(",");
                    if (isAsMapKey) {
                        selfType = sp[1].trim();
                    } else {
                        selfType = sp[2].trim();
                    }
                } else if (selfType.startsWith("list,")) {
                    String[] sp = selfType.split(",");
                    selfType = sp[1].trim();
                }
            }

            String toType = toCol.type;

            if (!selfType.equals(toType)) {
                throw new AssertionError(String.format("[%s].[%s] 类型为[%s] 设置外键到[%s].[%s] 类型为[%s]，类型不一致",
                        parentBean.fullName(), keys[i], selfType, fullRefTableName, toCols[i], toType));
            }
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
        String fullName = refFullName(parentBean, refName);
        if (parentBean.getDefine().allDefine.getBean(fullName) != null) {
            return fullName;
        }

        return refName;
    }

    public static String resolveTableFullName(Bean parentBean, String refName) {
        String fullName = refFullName(parentBean, refName);
        if (parentBean.getDefine().allDefine.getTable(fullName) != null) {
            return fullName;
        }
        return refName;
    }

    private static String refFullName(Bean parentBean, String refName) {
        if (refName.contains(".")) {
            return refName.startsWith(".") ? refName.substring(1) : refName;
        }

        String pkgName = parentBean.getPkgName();
        if (pkgName.isEmpty()) {
            return refName;
        }

        return pkgName + "." + refName;
    }

}
