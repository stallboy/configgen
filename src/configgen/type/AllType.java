package configgen.type;

import configgen.Node;
import configgen.define.Bean;
import configgen.define.AllDefine;
import configgen.define.Table;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class AllType extends Node {
    private final Map<String, TBean> tBeans = new LinkedHashMap<>();
    private final Map<String, TTable> tTables = new LinkedHashMap<>();

    public AllType(AllDefine allDefine) {
        super(null, "AllType");
        for (Bean bean : allDefine.getAllBeans()) {
            try {
                TBean tBean = new TBean(this, bean);
                tBeans.put(tBean.name, tBean);
            } catch (Throwable e) {
                throw new AssertionError(bean.name + "，这个结构体类型构造出错", e);
            }
        }

        for (Table table : allDefine.getAllTables()) {
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
}
