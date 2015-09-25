package configgen.value;

import configgen.CSV;
import configgen.Node;
import configgen.type.TList;
import configgen.type.TText;
import configgen.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VList extends Value {
    public final TList type;
    public final List<Value> list = new ArrayList<>();

    public VList(Node parent, String link, Value... vs) { // for keys and keysRef
        super(parent, link);
        type = null;
        for (Value v : vs) {
            list.add(v);
        }
    }

    public VList(Node parent, String link, TList type, List < Cell > data) {
        super(parent, link);
        this.type = type;

        List<Cell> sdata;
        if (type.count == 0) { //compress
            Assert(data.size() == 1);
            Cell dat = data.get(0);
            sdata = CSV.parseList(dat.data).stream().map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
        } else {
            sdata = data;
        }

        Assert(sdata.size() == type.columnSpan());
        int vc = type.value.columnSpan();
        int idx = 0;
        for (int i = 0; i < type.count; i++) {
            int s = i * vc;
            if (!sdata.get(s).data.isEmpty()) {
                list.add(Value.create(this, String.valueOf(idx), type.value, sdata.subList(s, s + vc)));
                idx++;
            } else {
                for (Cell dc : sdata.subList(s, s + vc)) {
                    Assert(dc.data.isEmpty(), "list value ignored by first cell empty, but part filled, " + dc);
                }
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof VList && type == ((VList) o).type && list.equals(((VList)o).list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

}
