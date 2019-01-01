package configgen.type;

import configgen.Logger;
import configgen.Node;
import configgen.define.Table;
import configgen.define.UniqueKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TTable extends Node {
    public final Table tableDefine;
    public final TBean tbean;
    public final Map<String, Type> primaryKey = new LinkedHashMap<>();
    public final List<Map<String, Type>> uniqueKeys = new ArrayList<>();

    public TTable(TDb parent, Table cfg) {
        super(parent, cfg.bean.name);
        this.tableDefine = cfg;
        tbean = new TBean(this, cfg.bean);
    }

    public void resolve() {
        tbean.resolve();
        if (tableDefine.enumType != Table.EnumType.None) {
            Type type = tbean.columns.get(tableDefine.enumStr);
            if (type == null) {
                error("枚举列未找到", tableDefine.enumStr);
            } else {
                require(type instanceof TString, "枚举列必须是字符串", tableDefine.enumStr, type);
            }
        }
        resolveKey(tableDefine.primaryKey, primaryKey);

        if (tableDefine.enumType != Table.EnumType.None) {
            require(primaryKey.size() == 1, "有枚举的表主键必须是自己或int");
            Type t = primaryKey.values().iterator().next();
            if (!tableDefine.isEnumAsPrimaryKey()) {
                require(t instanceof TInt, "有枚举的表主键必须是自己或int");
            }
        }

        for (UniqueKey uk : tableDefine.uniqueKeys.values()) {
            Map<String, Type> res = new LinkedHashMap<>();
            resolveKey(uk.keys, res);
            uniqueKeys.add(res);
        }
    }

    private void resolveKey(String[] keys, Map<String, Type> res) {
        for (String k : keys) {
            Type t = tbean.columns.get(k);
            if (t == null) {
                error("外键列未找到", k);
            } else if (t.hasText()) {
                Logger.verbose(fullName() + "的" + k + "有国际化字符串");
            }

            require(null == res.put(k, t), "外键列重复", k);
            require(keys.length == 1 || t instanceof TPrimitive, "外键类型不支持容器和Bean", k);
        }
    }
}
