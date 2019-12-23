package configgen.genlua;

import configgen.type.TBool;
import configgen.type.TInt;
import configgen.type.Type;
import configgen.value.*;

import java.util.*;

class CtxColumnStore {

    private boolean useColumnStore = false;
    private final Map<Integer, PackInfo> columnIdxToPackInfos = new LinkedHashMap<>();


    boolean isUseColumnStore() {
        return useColumnStore;
    }

    PackInfo getPackInfo(int columnIdx) {
        return columnIdxToPackInfos.get(columnIdx);
    }

    private static int MIN_ROW = 100;
    private static int MIN_SAVE = 100;

    static {
        String min_row = System.getProperty("genlua.column_min_row");
        if (min_row != null) {
            MIN_ROW = Integer.parseInt(min_row);
        }

        String min_save = System.getProperty("genlua.column_min_save");
        if (min_save != null) {
            MIN_SAVE = Integer.parseInt(min_save);
        }
    }

    void parseColumnStore(Ctx ctx) {
        VTable vTable = ctx.getVTable();
        if ((vTable.getVBeanList().size() - vTable.getTTable().getTBean().getColumns().size()) < MIN_ROW) {
            useColumnStore = false;
            return;
        }

        int can_save_cnt = 0;
        int rowCnt = vTable.getVBeanList().size();

        for (Type column : vTable.getTTable().getTBean().getColumns()) {
            boolean ok = false;
            int bitLen = 1;

            if (column instanceof TBool) {
                ok = true;
            } else if (column instanceof TInt) {
                bitLen = maxColumnValueIntLen(vTable, column);
                if (bitLen <= 26) {
                    ok = true;
                }
            }

            if (ok) {
                int rowsPerOne = 53 / bitLen;
                int save = rowCnt - (rowCnt + rowsPerOne - 1) / rowsPerOne;
                can_save_cnt += save;
                columnIdxToPackInfos.put(column.getColumnIndex(), new PackInfo(bitLen));
            }
        }

        if (can_save_cnt < MIN_SAVE) {
            useColumnStore = false;
            return;
        }

        AContext.getInstance().getStatistics().useColumnPack(can_save_cnt);
        useColumnStore = true;
    }

    private static final int MAX = 0x3ffffff; // 53/2=26 bit;　NOTE：　如果-1多也可以全bit为１表示－１，这里算了

    private static int maxColumnValueIntLen(VTable vTable, Type column) {
        int max = -1;
        for (VBean vBean : vTable.getVBeanList()) {
            Value v = vBean.getValues().get(column.getColumnIndex());
            if (v instanceof VInt) {
                int iv = ((VInt) v).value;

                if ((iv < 0) || (iv >= MAX)) {
                    //System.out.printf("%s不可压缩 %d\n", column.fullName(), iv);
                    return 32;
                } else if (iv > max) {
                    max = iv;
                }
            } else {
                throw new IllegalStateException("不该出现");
            }
        }
        return intLen(max);
    }

    private static int intLen(int v) {
        for (int i = 0; i <= 26; i++) {
            int cap = 1 << i;
            if (v <= cap - 1) {
                if (i == 0) {
                    return 1;
                } else {
                    return i;
                }
            }
        }
        throw new IllegalStateException("不该出现");
    }


}
