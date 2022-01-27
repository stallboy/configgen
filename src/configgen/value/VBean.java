package configgen.value;

import configgen.define.Bean;
import configgen.define.ForeignKey;
import configgen.type.TBean;
import configgen.type.TForeignKey;
import configgen.type.Type;

import java.util.*;

public class VBean extends VComposite {
    private final TBean tBean;
    private final List<Value> values;
    /**
     * 如果此Bean是基类，则childDynamicVBean对应具体子类Bean的值
     * 否则为null
     */
    private final VBean childDynamicVBean;

    VBean(TBean tbean, AData<TBean> adata) {
        super(tbean, adata.cells);
        tBean = tbean;
        require(adata.fullType.getBeanDefine().type == tBean.getBeanDefine().type, "类型应该一致");

        // 把pack的展开
        List<Cell> parsed;
        if (adata.packAsOne) { //先检测packAsOne，优先级高
            require(adata.cells.size() == 1, "pack应该只占一格");
            Cell dat = adata.cells.get(0);
            if (tBean.getBeanDefine().type == Bean.BeanType.BaseDynamicBean) {
                parsed = Cells.parseFunc(dat);
            } else {
                parsed = Cells.parseNestList(dat);
            }

        } else if (adata.fullType.getBeanDefine().isPackBySeparator) {
            require(adata.cells.size() == 1,
                    "pack的Bean应只占一格, （Bean定义时可指明是否要压缩成一格，后来修改设计为对column配置pack属性，更灵活）");
            Cell dat = adata.cells.get(0);
            parsed = Cells.parseList(dat, tBean.getBeanDefine().packSeparator);

        } else {
            require(adata.cells.size() == adata.fullType.columnSpan(), "列宽度应一致");
            require(adata.cells.size() >= tBean.columnSpan(), "列宽度不小于", tBean.columnSpan());
            parsed = adata.cells;
        }


        if (tBean.getBeanDefine().type == Bean.BeanType.BaseDynamicBean) {
            String childDynamicBeanName = parsed.get(0).data;
            if (childDynamicBeanName.isEmpty() && !tbean.getChildDynamicDefaultBeanName().isEmpty()) {
                childDynamicBeanName = tbean.getChildDynamicDefaultBeanName();
            }
            TBean fullChildTBean = adata.fullType.getChildDynamicBeanByName(childDynamicBeanName);
            require(Objects.nonNull(fullChildTBean), "子Bean不存在", childDynamicBeanName);
            int fullChildColumnSpan =  adata.packAsOne ? 1 : fullChildTBean.columnSpan();
            require(fullChildColumnSpan <= parsed.size() - 1, "数据子Bean大小应该小于", childDynamicBeanName);
            // 提取子Bean
            List<Cell> childCells = parsed.subList(1, fullChildColumnSpan + 1);
            AData<TBean> childAData = new AData<>(childCells, fullChildTBean, adata.packAsOne);
            TBean childTBean = tBean.getChildDynamicBeanByName(childDynamicBeanName);
            require(Objects.nonNull(childTBean), "子Bean不存在", childDynamicBeanName);
            childDynamicVBean = new VBean(childTBean, childAData);
            values = Collections.emptyList();

        } else {
            childDynamicVBean = null;
            values = new ArrayList<>(tBean.getColumnMap().size());
            int s = 0;
            for (Type columnFullType : adata.fullType.getColumns()) {
                int span = adata.packAsOne ? 1 : columnFullType.columnSpan();
                Type columnSelected = tBean.getColumn(columnFullType.name);
                if (columnSelected != null) {
                    // 提取单个field
                    Value v = Values.create(columnSelected, parsed.subList(s, s + span), columnFullType, adata.packAsOne);
                    values.add(v);
                }

                s += span;
            }
        }
    }

    public TBean getTBean() {
        return tBean;
    }

    public VBean getChildDynamicVBean() {
        return childDynamicVBean;
    }

    public List<Value> getValues() {
        return values;
    }

    Value getColumnValue(Type col) {
        return values.get(col.getColumnIndex());
    }


    @Override
    public void accept(ValueVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void verifyConstraint() {
        //检验真正子bean的约束
        if (childDynamicVBean != null) {
            childDynamicVBean.verifyConstraint();
            return;
        }

        //检验自己整体的Ref
        verifyRefs();

        //检验单个成员的约束
        for (Value value : values) {
            value.verifyConstraint();
        }

        //检验多个成员的Ref
        for (TForeignKey fk : tBean.getMRefs()) {
            if (isCellEmpty()) {
                require(fk.foreignKeyDefine.refType == ForeignKey.RefType.NULLABLE, "空数据，外键必须nullable", fk.foreignKeyDefine);
            } else {
                ArrayList<Value> vs = new ArrayList<>();
                for (Type col : fk.thisTableKeys) {
                    vs.add(values.get(col.getColumnIndex()));
                }
                VList keyValue = new VList(vs);

                if (fk.cache == null) {
                    VTable vtable = AllValue.getCurrent().getVTable(fk.refTable.name);
                    fk.cache = fk.foreignKeyDefine.ref.refToPrimaryKey() ? vtable.primaryKeyValueSet : vtable.uniqueKeyValueSetMap.get(String.join(",", fk.foreignKeyDefine.ref.cols));
                }
                require(fk.cache.contains(keyValue), "外键未找到", fk.refTable, keyValue);
            }
        }
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
