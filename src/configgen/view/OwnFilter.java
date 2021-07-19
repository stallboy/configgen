package configgen.view;

import configgen.define.AllDefine;
import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.Table;

/**
 * 根据Own关键字定义的过滤器
 */
public class OwnFilter implements ViewFilter {
    final String own;

    public OwnFilter(String own) {
        this.own = own;
    }

    @Override
    public String name() {
        return "own(" + own + ")";
    }

    @Override
    public boolean acceptColumn(Column column) {
        return acceptOwn(column.own);
    }

    @Override
    public boolean acceptBean(Bean bean) {
        switch (bean.type) {
            case BaseDynamicBean:
                return acceptOwn(bean.own);
            case ChildDynamicBean:
                // ChildDynamicBean一旦需要，就算没有列，其实也隐含了枚举字符串，所以要包含上
                return true;
            default:
                for (Column column : bean.columns.values()) {
                    if (acceptColumn(column)) {
                        return true;
                    }
                }
                return false;
        }
    }

    @Override
    public boolean acceptTable(Table table) {
        return acceptBean(table.bean);
    }

    private boolean acceptOwn(String defineOwn) {
        if (own == null || own.isEmpty()) {
            return true;
        }
        if (defineOwn == null || defineOwn.isEmpty()) {
            return false;
        }
        for (String oneOwn : defineOwn.split(",")) {
            if (own.equals(oneOwn)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void saveToXml(AllDefine allDefine) {

    }

    @Override
    public String toString() {
        return name();
    }
}
