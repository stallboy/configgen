package configgen.value;

import configgen.type.Type;

import java.util.List;

/**
 * 作为vbean，vlist，vmap构造时的参数传入，作为数据来源
 */
class AData<T extends Type> {
    /**
     * 构造value时，传入的是subType，再结合这里的fullType，和cells可以提取出需要的cell
     */
    final List<Cell> cells;
    final T fullType;

    /**
     * 因为packAsOne的传递特性，所以这里要包含
     */
    final boolean packAsOne;


    AData(List<Cell> cells, T fullType, boolean packAsOne) {
        this.cells = cells;
        this.fullType = fullType;
        this.packAsOne = packAsOne;
    }
}
