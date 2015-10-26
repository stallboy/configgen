package configgen.value;

import configgen.Node;
import configgen.data.CSV;
import configgen.type.MRef;
import configgen.type.TBean;
import configgen.type.Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VBean extends Value {
    public final TBean tbean;
    public final Map<String, Value> map = new LinkedHashMap<>();

    public VBean(Node parent, String name, TBean type, List<Cell> data) {
        super(parent, name, type, data);
        this.tbean = type;

        List<Cell> parsed;
        if (type.define.compress) {
            require(data.size() == 1);
            Cell dat = data.get(0);
            parsed = CSV.parseList(dat.data).stream().map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
        } else {
            require(data.size() == type.columnSpan());
            parsed = data;
        }

        int s = 0;
        for (Map.Entry<String, Type> e : type.fields.entrySet()) {
            String n = e.getKey();
            Type t = e.getValue();
            int span = t.columnSpan();
            map.put(n, Value.create(this, n, t, parsed.subList(s, s + span)));
            s += span;
        }
    }

    @Override
    public void accept(ValueVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void verifyConstraint() {
        verifyRefs();

        map.values().forEach(Value::verifyConstraint);

        for (MRef mr : tbean.mRefs) {
            List<Value> vs = new ArrayList<>();
            for (String k : mr.define.keys) {
                vs.add(map.get(k));
            }
            VList key = new VList(this, "__ref_" + mr.define.name, vs);
            if (mr.ref != null) {
                if (isNull()) {
                    require(mr.define.nullable, key.toString(), "null not support ref", mr.ref.fullName());
                } else {
                    require(((CfgVs) root).cfgvs.get(mr.ref.name).vkeys.contains(key), key.toString(), "not found in ref", mr.ref.fullName());
                }
            }
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
