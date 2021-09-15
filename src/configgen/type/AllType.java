package configgen.type;

import configgen.Node;
import configgen.define.AllDefine;
import configgen.define.Bean;
import configgen.define.Ref;
import configgen.view.DefineView;
import configgen.define.Table;

import java.util.*;

public class AllType extends Node {
    public final Map<String, TBean> tBeans = new LinkedHashMap<>();
    public final Map<String, TTable> tTables = new LinkedHashMap<>();

    public AllType(DefineView defineView) {
        super(null, "AllType");

        for (Bean bean : defineView.beans.values()) {
            try {
                TBean tBean = new TBean(this, bean);
                tBeans.put(tBean.name, tBean);
            } catch (Throwable e) {
                throw new AssertionError(bean.name + "，这个结构体类型构造出错", e);
            }
        }

        for (Table table : defineView.tables.values()) {
            try {
                TTable tTable = new TTable(this, table);
                tTables.put(tTable.name, tTable);
            } catch (Throwable e) {
                throw new AssertionError(table.name + "，这个表类型构造出错", e);
            }
        }
    }

    public Collection<TBean> getTBeans() {
        return tBeans.values();
    }

    public Collection<TTable> getTTables() {
        return tTables.values();
    }

    public TBean getTBean(String beanName) {
        return tBeans.get(beanName);
    }

    public TTable getTTable(String tableName) {
        return tTables.get(tableName);
    }

    public void resolve() {
        for (TBean tBean : tBeans.values()) {
            try {
                tBean.resolve();
            } catch (Throwable e) {
                throw new AssertionError(tBean.name + ",这个结构体类型解析出错", e);
            }
        }

        for (TTable tTable : tTables.values()) {
            try {
                tTable.resolve();
            } catch (Throwable e) {
                throw new AssertionError(tTable.name + ",这个表类型解析出错", e);
            }
        }
    }

    TBean resolveBeanRef(TBean parent, String refBeanName) {
        refBeanName = Ref.resolveBeanFullName(parent.getBeanDefine(), refBeanName);
        return getTBean(refBeanName);
    }

    TTable resolveTableRef(TBean parent, String refTableName) {
        refTableName = Ref.resolveTableFullName(parent.getBeanDefine(), refTableName);
        return getTTable(refTableName);
    }

}
