package configgen.value;

import configgen.Node;
import configgen.data.CSV;
import configgen.define.Bean;
import configgen.define.ForeignKey;
import configgen.type.TBean;
import configgen.type.TForeignKey;
import configgen.type.Type;

import java.util.*;
import java.util.stream.Collectors;

public class VBean extends Value {
    public final TBean beanType;
    public final Map<String, Value> valueMap = new LinkedHashMap<>();

    public final VBean actionVBean;

    public VBean(Node parent, String name, TBean tbean, List<Cell> data) {
        super(parent, name, tbean, data);
        beanType = tbean;
        if (beanType.beanDefine.type == Bean.BeanType.BaseAction) {
            String actionName = data.get(0).data;
            TBean actionBean = beanType.actionBeans.get(actionName);
            actionVBean = new VBean(this, actionName, actionBean, data.subList(1, data.size()));
        } else {
            actionVBean = null;
            List<Cell> parsed;
            if (beanType.beanDefine.compress) {
                require(data.size() == 1);
                Cell dat = data.get(0);
                parsed = CSV.parseList(dat.data, beanType.beanDefine.compressSeparator).stream().map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
            } else {
                if (beanType.beanDefine.type == Bean.BeanType.Action){
                    require(data.size() >= beanType.columnSpan());
                    parsed = data.subList(0, beanType.columnSpan());
                }else{
                    require(data.size() == beanType.columnSpan());
                    parsed = data;
                }
            }
            int s = 0;
            for (Map.Entry<String, Type> e : beanType.columns.entrySet()) {
                String n = e.getKey();
                Type t = e.getValue();
                int span = t.columnSpan();
                valueMap.put(n, Value.create(this, n, t, parsed.subList(s, s + span)));
                s += span;
            }
        }
    }

    @Override
    public void accept(ValueVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void verifyConstraint() {
        if (beanType.beanDefine.type == Bean.BeanType.BaseAction) {
            actionVBean.verifyConstraint();
        } else {
            verifyRefs();
            valueMap.values().forEach(Value::verifyConstraint);
            for (TForeignKey fk : beanType.mRefs) {
                List<Value> vs = new ArrayList<>();
                for (String k : fk.foreignKeyDefine.keys) {
                    vs.add(valueMap.get(k));
                }
                VList keyValue = new VList(this, "__ref_" + fk.foreignKeyDefine.name, vs);
                if (isNull()) {
                    require(fk.foreignKeyDefine.refType == ForeignKey.RefType.NULLABLE, keyValue.toString(), "null not support ref", fk.foreignKeyDefine.fullName());
                } else {
                    VTable vtable = ((VDb) root).vtables.get(fk.refTable.name);
                    Set<Value> keyValueSet;
                    if (fk.foreignKeyDefine.ref.refToPrimaryKey()) {
                        keyValueSet = vtable.primaryKeyValueSet;
                    } else {
                        keyValueSet = vtable.uniqueKeyValueSetMap.get(String.join(",", fk.foreignKeyDefine.ref.cols));
                    }
                    require(keyValueSet.contains(keyValue), keyValue.toString(), "not found in ref", fk.refTable.fullName());
                }
            }
        }

    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof VBean && type == ((VBean) o).type && valueMap.equals(((VBean) o).valueMap);
    }

    @Override
    public int hashCode() {
        return valueMap.hashCode();
    }
}
