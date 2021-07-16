package configgen.define;

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

    boolean valid(DefineView defineView) {
        Table t = defineView.tables.get(table);
        return t != null && t.bean.columns.keySet().containsAll(Arrays.asList(cols));
    }

    boolean equal(Ref ref){
        return ref != null && table.equals(ref.table) && Arrays.equals(cols, ref.cols);
    }
}
