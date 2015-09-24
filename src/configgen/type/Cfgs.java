package configgen.type;

import configgen.Node;
import configgen.define.ConfigCollection;

import java.util.HashMap;
import java.util.Map;

public class Cfgs extends Node {
    public final ConfigCollection define;
    public final Map<String, TBean> tbeans = new HashMap<>();
    public final Map<String, Cfg> cfgs = new HashMap<>();

    public Cfgs(ConfigCollection cc) {
        super(null, "");
        define = cc;
        define.beans.forEach((k, v) -> tbeans.put(k, new TBean(this, v)));
        define.configs.forEach((k, v) -> cfgs.put(k, new Cfg(this, v)));
    }

    public void resolve() {
        tbeans.values().forEach(TBean::resolve);
        cfgs.values().forEach(Cfg::resolve);
    }
}
