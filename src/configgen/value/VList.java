package configgen.value;

import configgen.define.Column;
import configgen.type.TList;
import configgen.util.CSV;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VList extends VComposite {
    private ArrayList<Value> list;

    VList(TList type, List<Cell> data, boolean compressAsOne) {
        super(type, data);

        List<Cell> parsed;
        if (compressAsOne || type.compressType == Column.CompressType.AsOne) {
            require(data.size() == 1);
            Cell dat = data.get(0);
            parsed = CSV.parseNestList(dat.data).stream().map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
        } else if (type.compressType == Column.CompressType.UseSeparator) {
            require(data.size() == 1);
            Cell dat = data.get(0);
            parsed = CSV.parseList(dat.data, type.compressSeparator).stream().map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
        } else {
            require(data.size() == type.columnSpan());
            parsed = data;
        }

        list = new ArrayList<>();
        int vc = compressAsOne ? 1 : type.value.columnSpan();  // 注意这里compressAsOne的自上而下一直传递的特性
        for (int s = 0; s < parsed.size(); s += vc) {
            if (!parsed.get(s).data.isEmpty()) { //first as a null clue, see code generator
                list.add(Value.create(type.value, parsed.subList(s, s + vc), compressAsOne));
            } else {
                for (Cell dc : parsed.subList(s, s + vc)) {
                    require(dc.data.isEmpty(), "list的item第一个为空格后，之后必须也都是空格", dc);
                }
            }
        }

        list.trimToSize();
    }

    VList(ArrayList<Value> vs) { // for primaryKey and keysRef
        super(null, toRaw(vs));
        list = vs;
    }


    private static List<Cell> toRaw(List<Value> vs) {
        List<Cell> res = new ArrayList<>(vs.size());
        for (Value v : vs) {
            v.collectCells(res);
        }
        return res;
    }

    public List<Value> getList() {
        return list;
    }

    @Override
    public void accept(ValueVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void verifyConstraint() {
        for (Value value : list) {
            value.verifyConstraint();
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof VList && list.equals(((VList) o).list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }


}
