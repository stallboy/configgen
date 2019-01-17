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
    public final VBean childDynamicVBean;

    VBean(TBean tbean, List<Cell> data, boolean compressAsOne) {
        super(tbean, data);
        beanType = tbean;

        List<Cell> parsed;
        if (compressAsOne) {
            require(data.size() == 1, "compressAsOne应该只占一格");
            Cell dat = data.get(0);
            if (beanType.beanDefine.type == Bean.BeanType.BaseDynamicBean) {
                parsed = CSV.parseFunction(dat.data).stream().map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
            } else {
                parsed = CSV.parseNestList(dat.data).stream().map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
            }
        } else if (beanType.beanDefine.compress) {
            require(data.size() == 1, "compress的Bean应该只占一格");
            Cell dat = data.get(0);
            parsed = CSV.parseList(dat.data, beanType.beanDefine.compressSeparator).stream().map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());

        } else if (beanType.beanDefine.type == Bean.BeanType.ChildDynamicBean) {
            require(data.size() >= beanType.columnSpan(), "动态子Bean占格子数要<=基类Bean计算的格子数");
            parsed = data.subList(0, beanType.columnSpan());

        } else {
            require(data.size() == beanType.columnSpan(), "列宽度应该等于", beanType.columnSpan());
            parsed = data;
        }

        if (beanType.beanDefine.type == Bean.BeanType.BaseDynamicBean) {
            String childDynamicBeanName = data.get(0).data;
            TBean childTBean = beanType.childDynamicBeans.get(childDynamicBeanName);
            require(Objects.nonNull(childTBean), "子Bean不存在", childDynamicBeanName);
            childDynamicVBean = new VBean(childTBean, data.subList(1, data.size()), compressAsOne);
            values = new ArrayList<>();
        } else {
            childDynamicVBean = null;
            values = new ArrayList<>(beanType.columns.size());
            int s = 0;
            for (Type t : beanType.getColumns()) {
                int span = compressAsOne ? 1 : t.columnSpan();
                Value v = Value.create(t, parsed.subList(s, s + span), compressAsOne);
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
        if (beanType.beanDefine.type == Bean.BeanType.BaseDynamicBean) {
            childDynamicVBean.verifyConstraint();
        } else {
            verifyRefs();
            for (Value value : values) {
                value.verifyConstraint();
            }
            for (TForeignKey fk : beanType.mRefs) {
                ArrayList<Value> vs = new ArrayList<>();
                for (String k : fk.foreignKeyDefine.keys) {
                    vs.add(getColumnValue(k));
                }
                VList keyValue = new VList(vs);
                if (isCellEmpty()) {
                    require(fk.foreignKeyDefine.refType == ForeignKey.RefType.NULLABLE, "空数据，外键必须nullable", fk.foreignKeyDefine);
                } else {
                    if (fk.cache == null) {
                        VTable vtable = VDb.getCurrent().getVTable(fk.refTable.name);
                        fk.cache = fk.foreignKeyDefine.ref.refToPrimaryKey() ? vtable.primaryKeyValueSet : vtable.uniqueKeyValueSetMap.get(String.join(",", fk.foreignKeyDefine.ref.cols));
                    }
                    require(fk.cache.contains(keyValue), "外键未找到", fk.refTable, keyValue);
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
