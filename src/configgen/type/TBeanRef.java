package configgen.type;

import configgen.Node;

public class TBeanRef extends Type {
    public final TBean tBean;
    public final boolean packAsOne; //这里只考虑支持AsOne这一种类型

    TBeanRef(Node parent, String name, int idx, TBean tBean, boolean packAsOne) {
        super(parent, name, idx);
        this.tBean = tBean;
        this.packAsOne = packAsOne;
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
    public boolean hasBlock() {
        return tBean.hasBlock();
    }

    @Override
    public int columnSpan() {
        return packAsOne ? 1 : tBean.columnSpan();
    }

    @Override
    public <T> T accept(TypeVisitorT<T> visitor) {
        return visitor.visit(this);
    }
}
