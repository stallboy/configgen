package configgen.genlua;

import configgen.Logger;

class AStat {

    private int emptyTableCount = 0;
    private int sharedTableReduceCount = 0;
    private int packBoolReduceCount = 0;
    private int columnPackSaveCount = 0;
    private int columnTableCnt = 0;

    void useEmptyTable() {
        emptyTableCount++;
    }

    void useSharedTable(int c) {
        sharedTableReduceCount += c;
    }

    void usePackBool(int c) {
        packBoolReduceCount += c;
    }

    void useColumnPack(int c) {
        columnPackSaveCount += c;
        columnTableCnt++;
    }

    void print() {
        Logger.log(String.format("可共享空table个数:%d, 共享table节省:%d，压缩bool节省:%d, 列模式可省:%d(%d个表)",
                emptyTableCount,
                sharedTableReduceCount,
                packBoolReduceCount,
                columnPackSaveCount, columnTableCnt));
    }
}
