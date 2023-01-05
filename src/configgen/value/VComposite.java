package configgen.value;

import configgen.type.Type;

import java.util.List;
/**
 * VComposite的子类要override equals和hashCode，便于genlua.ValueSharedLayer来统计
 */
public abstract class VComposite extends Value {
    protected List<Cell> cells;

    VComposite(Type type, List<Cell> _cells) {
        super(type);
        cells = _cells;
    }

    /**
     * shared用于lua生成时最小化内存占用，所以对同一个表中相同的table，就共享， 算是个优化
     */
    private boolean shared = false;

    public void setShared() {
        shared = true;
    }

    public boolean isShared() {
        return shared;
    }
    
    @Override
    public boolean isCellEmpty() {
        for (Cell cell : cells) {
            if (!cell.getData().trim().isEmpty())
                return false;
        }
        return true;
    }


    @Override
    public void collectCells(List<Cell> targetCells) {
        targetCells.addAll(cells);
    }

    @Override
    public String toString() {
        if (!cells.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(cells.get(0).toString());
            for (int i = 1; i < cells.size(); i++) {
                sb.append(";").append(cells.get(i).getData());
            }
            return sb.toString();
        }
        return "";
    }
}
