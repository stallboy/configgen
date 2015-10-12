package configgen.type;

import configgen.Node;
import configgen.define.ConfigCollection;

import java.util.Map;
import java.util.TreeMap;

public class Cfgs extends Node {
    public final ConfigCollection define;
    public final Map<String, TBean> tbeans = new TreeMap<>();
    public final Map<String, Cfg> cfgs = new TreeMap<>();

    public Cfgs(ConfigCollection cc) {
        super(null, "type");
        define = cc;
        define.beans.forEach((k, v) -> tbeans.put(k, new TBean(this, v)));
        define.configs.forEach((k, v) -> cfgs.put(k, new Cfg(this, v)));
    }

    public void resolve() {
        tbeans.values().forEach(TBean::resolve);
        cfgs.values().forEach(Cfg::resolve);
    }
}
