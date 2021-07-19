package configgen.view;

import configgen.define.AllDefine;
import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.Table;

public abstract class AbstractFilter implements ViewFilter {
    protected ViewFilter parent;

    void setParent(ViewFilter parent) {
        this.parent = parent;
    }

    @Override
    public boolean acceptColumn(Column column) {
        if (parent != null && !parent.acceptColumn(column)) {
            return false;
        }

        return doAcceptColumn(column);
    }

    @Override
    public boolean acceptBean(Bean bean) {
        if (parent != null && !parent.acceptBean(bean)) {
            return false;
        }

        return doAcceptBean(bean);
    }

    @Override
    public boolean acceptTable(Table table) {
        if (parent != null && !parent.acceptTable(table)) {
            return false;
        }

        return doAcceptTable(table);
    }

    @Override
    public void saveToXml(AllDefine allDefine) {
        if (parent != null) {
            parent.saveToXml(allDefine);
        }

        doSaveToXml(allDefine);
    }

    @Override
    public String toString() {
        return name();
    }

    protected abstract boolean doAcceptColumn(Column column);

    protected abstract boolean doAcceptBean(Bean bean);

    protected abstract boolean doAcceptTable(Table table);

    protected abstract void doSaveToXml(AllDefine allDefine);

}
