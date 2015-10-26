package configgen.type;

import configgen.Node;
import configgen.define.Config;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cfg extends Node {
    public final Config define;
    public final TBean tbean;
    public final Map<String, Type> keys = new LinkedHashMap<>();

    public Cfg(Cfgs parent, Config cfg) {
        super(parent, cfg.bean.name);
        this.define = cfg;
        tbean = new TBean(this, cfg.bean);
    }

    public void resolve() {
        tbean.resolve();

        if (!define.enumStr.isEmpty()) {
            Type type = tbean.fields.get(define.enumStr);
            require(type != null, "enum not found", define.enumStr);
            require(type instanceof TString, "enum type not string", type.toString());
        }
        if (define.keys.length > 0) {
            for (String k : define.keys) {
                Type t = tbean.fields.get(k);
                require(t != null, "primary keys not found", k, String.join(",", define.keys));
                require(null == keys.put(k, t), "primary keys duplicate", k);
                require(t instanceof TPrimitive, "primary keys not support bean and container", k);
            }
        } else {
            Map.Entry<String, Type> k = tbean.fields.entrySet().iterator().next();
            keys.put(k.getKey(), k.getValue());
            require(k.getValue() instanceof TPrimitive || k.getValue() instanceof TBean, "primary key not supported container");
        }
    }
}
