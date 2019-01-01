package configgen.value;

import configgen.type.Type;

import java.util.List;

public abstract class VComposite extends Value {
    private final List<Cell> cells;

    VComposite(Type type, List<Cell> data) {
        super(type);
        cells = data;
    }

    @Override
    public boolean isCellEmpty() {
        for (Cell cell : cells) {
            if (!cell.data.trim().isEmpty())
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
                sb.append(",").append(cells.get(i).data);
            }
            return sb.toString();
        }
        return "";
    }


}
