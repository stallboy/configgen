package configgen.type;

import configgen.Node;
import configgen.define.Bean;
import configgen.define.Db;
import configgen.define.Table;

import java.util.LinkedHashMap;
import java.util.Map;

public class TDb extends Node {
    public final Db dbDefine;
    public final Map<String, TBean> tbeans = new LinkedHashMap<>();
    public final Map<String, TTable> ttables = new LinkedHashMap<>();

    public TDb(Db def) {
        super(null, "tdb");
        dbDefine = def;
        for (Bean bean : dbDefine.beans.values()) {
            try {
                TBean tBean = new TBean(this, bean);
                tbeans.put(bean.name, tBean);
            } catch (Throwable e) {
                throw new AssertionError(bean.name + "，这个结构体类型构造出错", e);
            }
        }

        for (Table table : dbDefine.tables.values()) {
            try {
                TTable tTable = new TTable(this, table);
                ttables.put(table.name, tTable);
            } catch (Throwable e) {
                throw new AssertionError(table.name + "，这个表类型构造出错", e);
            }
        }
    }

    public void resolve() {
        for (TBean tBean : tbeans.values()) {
            try {
                tBean.resolve();
            } catch (Throwable e) {
                throw new AssertionError(tBean.name + ",这个结构体类型解析出错", e);
            }
        }

        for (TTable tTable : ttables.values()) {
            try {
                tTable.resolve();
            } catch (Throwable e) {
                throw new AssertionError(tTable.name + ",这个表类型解析出错", e);
            }
        }
    }
}
