package configgen.type;

import configgen.Node;
import configgen.define.Config;
import configgen.value.CfgV;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cfg extends Node {
    public final Config define;
    public final TBean tbean;
    public final Map<String, Type> keys = new LinkedHashMap<>();

    public CfgV value; //set by CfgV

    public Cfg(Cfgs parent, Config cfg) {
        super(parent, cfg.bean.name);
        this.define = cfg;
        tbean = new TBean(this, cfg.bean);
    }

    public void resolve() {
        tbean.resolve();

        if (!define.enumStr.isEmpty()) {
            Type type = tbean.fields.get(define.enumStr);
            Assert(type != null, "enum not found", define.enumStr);
            Assert(type instanceof TString, "enum type not string", type.toString());
        }

        if (define.keys.length > 0) {
            for (String k : define.keys) {
                Type t = tbean.fields.get(k);
                Assert(t != null, "primary keys not found", k, String.join(",", define.keys));
                Assert(null == keys.put(k, t), "primary keys duplicate", k);
                Assert(t instanceof TPrimitive, "primary keys not support bean and container", k);
            }
        } else {
            Map.Entry<String, Type> k = tbean.fields.entrySet().iterator().next();
            keys.put(k.getKey(), k.getValue());
            Assert(k.getValue() instanceof TPrimitive || k.getValue() instanceof TBean, "primary key not supported container");
        }
    }
}
