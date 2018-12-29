package configgen.value;

import configgen.define.Bean;
import configgen.define.ForeignKey;
import configgen.type.TBean;
import configgen.type.TForeignKey;
import configgen.type.Type;
import configgen.util.CSV;

import java.util.*;
import java.util.stream.Collectors;

public class VBean extends VComposite {
    public final TBean beanType;
    private List<Value> values;
    public final VBean actionVBean;


    VBean(TBean tbean, List<Cell> data) {
        super(tbean, data);
        beanType = tbean;
        if (beanType.beanDefine.type == Bean.BeanType.BaseAction) {
            String actionName = data.get(0).data;
            TBean actionBean = beanType.actionBeans.get(actionName);
            require(Objects.nonNull(actionBean), actionName + " not exist");
            actionVBean = new VBean(actionBean, data.subList(1, data.size()));
            values = new ArrayList<>();
        } else {
            actionVBean = null;
            List<Cell> parsed;
            if (beanType.beanDefine.compress) {
                require(data.size() == 1);
                Cell dat = data.get(0);
                parsed = CSV.parseList(dat.data, beanType.beanDefine.compressSeparator).stream().map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
            } else {
                if (beanType.beanDefine.type == Bean.BeanType.Action) {
                    require(data.size() >= beanType.columnSpan());
                    parsed = data.subList(0, beanType.columnSpan());
                } else {
                    require(data.size() == beanType.columnSpan(), " columns should equals " + beanType.columnSpan());
                    parsed = data;
                }
            }

            values = new ArrayList<>(beanType.columns.size());
            int s = 0;
            for (Map.Entry<String, Type> e : beanType.columns.entrySet()) {
                Type t = e.getValue();
                int span = t.columnSpan();
                Value v = Value.create(t, parsed.subList(s, s + span));
                values.add(v);
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
            values.forEach(Value::verifyConstraint);
            for (TForeignKey fk : beanType.mRefs) {
                ArrayList<Value> vs = new ArrayList<>();
                for (String k : fk.foreignKeyDefine.keys) {
                    vs.add(getColumnValue(k));
                }
                VList keyValue = new VList(vs);
                if (isCellEmpty()) {
                    require(fk.foreignKeyDefine.refType == ForeignKey.RefType.NULLABLE, keyValue.toString(), "空数据，外键必须nullable", fk.foreignKeyDefine.fullName());
                } else {
                    VTable vtable = VDb.getCurrent().getVTable(fk.refTable.name);
                    Set<Value> keyValueSet;
                    if (fk.foreignKeyDefine.ref.refToPrimaryKey()) {
                        keyValueSet = vtable.primaryKeyValueSet;
                    } else {
                        keyValueSet = vtable.uniqueKeyValueSetMap.get(String.join(",", fk.foreignKeyDefine.ref.cols));
                    }
                    require(keyValueSet.contains(keyValue), keyValue.toString(), "外键未找到", fk.refTable.fullName());
                }
            }
        }

    }

    Value getColumnValue(String col) {
        return values.get(beanType.getColumnIndex(col));
    }

    public Collection<Value> getValues() {
        return values;
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof VBean && type == ((VBean) o).type && values.equals(((VBean) o).values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }


}
