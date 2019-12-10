package configgen.value;

import configgen.type.Type;

import java.util.List;

class AData<T extends Type> {
    // 一个fullType对应一系列数据cells，从VTable开始
    final List<Cell> cells;
    final T fullType;
    final boolean compressAsOne;


    AData(List<Cell> cells, T fullType, boolean compressAsOne) {
        this.cells = cells;
        this.fullType = fullType;
        this.compressAsOne = compressAsOne;
    }

    boolean isCompressAsOne() {
        return compressAsOne;
    }
}
