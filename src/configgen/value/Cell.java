package configgen.value;

import configgen.data.DSheet;

/**
 * 保留原始信息，用于打印错误信息时，指出具体时那一行那一列出错
 */
public final class Cell {
    final DSheet sheet;
    final int row;  // row 从0开始,  sheet.recordList.get(row).get(col) 就返回对应格子的string
    final int col;  // col 从0开始
    final String data;

    public Cell(DSheet sheet, int row, int col, String data) {
        this.sheet = sheet;
        this.row = row;
        this.col = col;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        int r = row;
        int c = col;
        if (sheet.isColumnMode()) {
            r = col;
            c = row;
        }
        return String.format("sheet=%s,row=%d,col=%s,data=%s", sheet.name, DSheet.getHeadRow() + r + 1, toAZ(c), data);
    }

    private static final int N = 'Z' - 'A' + 1;

    private static String toAZ(int v) {
        int q = v / N;
        String r = String.valueOf((char) ('A' + (v % N)));
        if (q > 0) {
            return toAZ(q) + r;
        } else {
            return r;
        }
    }

}
