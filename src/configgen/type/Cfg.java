package configgen.type;

import configgen.Node;
import configgen.define.Config;

import java.util.HashSet;
import java.util.Set;

public class Cfg extends Node {
    public final Config define;
    public final TBean tbean;

    public Cfg(Cfgs parent, Config cfg) {
        super(parent, cfg.bean.name);
        this.define = cfg;
        tbean = new TBean(this, cfg.bean);
    }

    void resolve() {
        tbean.resolve();
        if (!define.enumStr.isEmpty()) {
            Type type = tbean.fields.get(define.enumStr);
            define.Assert(type != null, "enum not found", define.enumStr);
            define.Assert(type instanceof TString, "enum type not string", type.toString());
        }

        Set<String> keys = new HashSet<>();
        for (String k : define.keys) {
            Type t = tbean.fields.get(k);
            define.Assert(t != null, "primary keys not found", k);
            define.Assert(keys.add(k), "primary keys duplicate", k);
            define.Assert(t instanceof TPrimitive, "primary keys not support bean and container", k);
        }

        Type t = tbean.fields.values().iterator().next();
        define.Assert(t instanceof TPrimitive || t instanceof TBean, "primary key not supported container");
    }
}
