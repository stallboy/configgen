package configgen.value;

import configgen.data.DSheet;

public class SubCell extends Cell {

    SubCell(DSheet sheet, int row, int col, String data) {
        super(sheet, row, col, data);
    }

    @Override
    public boolean isRootAndEmpty() {
        return false;
    }
}
