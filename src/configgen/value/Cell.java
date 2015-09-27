package configgen.value;

public class Cell {
    public final int row;
    public final int col;
    public final String data;

    public Cell(int row, int col, String data) {
        this.row = row;
        this.col = col;
        this.data = data;
    }

    @Override
    public String toString() {
        return "row=" + (row+1) + ",col=" + toAZ(col) + ",data=" + data;
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
