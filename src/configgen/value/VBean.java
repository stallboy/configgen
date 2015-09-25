package configgen.value;

import configgen.CSV;
import configgen.Node;
import configgen.type.TBean;
import configgen.type.Type;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VBean extends Value {
    public final TBean type;
    public final Map<String, Value> map = new LinkedHashMap<>();

    public VBean(Node parent, String link, TBean type, List<Cell> data) {
        super(parent, link);
        this.type = type;

        List<Cell> sdata;
        if (type.define.compress) {
            Assert(data.size() == 1);
            Cell dat = data.get(0);
            sdata = CSV.parseList(dat.data).stream().map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
        } else {
            sdata = data;
        }

        Assert(sdata.size() == type.columnSpan());
        int s = 0;
        for (Map.Entry<String, Type> e : type.fields.entrySet()) {
            String name = e.getKey();
            Type t = e.getValue();
            int span = t.columnSpan();
            map.put(name, Value.create(this, name, t, sdata.subList(s, s + span)));
            s += span;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof VBean && type == ((VBean) o).type && map.equals(((VBean) o).map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

}
