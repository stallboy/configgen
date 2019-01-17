package configgen.type;

import configgen.Node;
import configgen.define.Bean;
import configgen.define.Db;
import configgen.define.Table;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TDb extends Node {
    private final Map<String, TBean> tBeans = new LinkedHashMap<>();
    public final Map<String, TTable> tTables = new LinkedHashMap<>();

    public TDb(Db defineDb) {
        super(null, "tdb");
        for (Bean bean : defineDb.getBeans()) {
            try {
                TBean tBean = new TBean(this, bean);
                tBeans.put(bean.name, tBean);
            } catch (Throwable e) {
                throw new AssertionError(bean.name + "，这个结构体类型构造出错", e);
            }
        }

        for (Table table : defineDb.tables.values()) {
            try {
                TTable tTable = new TTable(this, table);
                tTables.put(table.name, tTable);
            } catch (Throwable e) {
                throw new AssertionError(table.name + "，这个表类型构造出错", e);
            }
        }
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

    public TBean getTBean(String beanName){
        return tBeans.get(beanName);
    }

    public Collection<TBean> getTBeans() {
        return tBeans.values();
    }

    public Collection<TTable> getTTables() {
        return tTables.values();
    }
}
