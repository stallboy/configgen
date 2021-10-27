package configgen.value;

import configgen.data.DSheet;

/**
 * 保留原始信息，用于打印错误信息时，指出具体时那一行那一列出错
 */
public final class Cell {
    final DSheet sheet;
    final int row;
    final int col;
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
        return String.format("sheet=%s,row=%d,col=%s,data=%s", sheet.name, row, toAZ(col), data);
    }

    private static final int N = 'Z' - 'A' + 1;

    private static String toAZ(int v) {
        int q = v / N;
        String r = String.valueOf((char) ('A' + (v % N)));
        if (q > 0)
            return toAZ(q) + r;
        else
            return r;
    }

}
