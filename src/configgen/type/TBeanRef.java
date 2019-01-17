package configgen.type;

import configgen.Node;

public class TBeanRef extends Type {
    public final TBean tBean;
    public final boolean compressAsOne; //这里只考虑支持AsOne这一种类型

    TBeanRef(Node parent, String name, int idx, Constraint cons, TBean tBean, boolean compressAsOne) {
        super(parent, name, idx, cons);
        this.tBean = tBean;
        this.compressAsOne = compressAsOne;
    }

    @Override
    public boolean hasRef() {
        return tBean.hasRef();
    }

    @Override
    public boolean hasSubBean() {
        return tBean.hasSubBean();
    }

    @Override
    public boolean hasText() {
        return tBean.hasText();
    }

    @Override
    public int columnSpan() {
        return compressAsOne ? 1 : tBean.columnSpan();
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(TypeVisitorT<T> visitor) {
        return visitor.visit(this);
    }
}
