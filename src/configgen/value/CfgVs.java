package configgen.value;

import configgen.Node;
import configgen.data.Datas;
import configgen.type.Cfgs;

import java.util.LinkedHashMap;
import java.util.Map;

public class CfgVs extends Node {
    public final Cfgs cfgs;
    public final Datas datas;
    public final Map<String, CfgV> cfgvs = new LinkedHashMap<>();

    public CfgVs(Cfgs cfgs, Datas datas) {
        super(null, "value");
        this.cfgs = cfgs;
        this.datas = datas;

        cfgs.cfgs.forEach((name, cfg)->{
            CfgV c = new CfgV(this, name, cfg, datas.datas.get(name));
            cfgvs.put(name, c);
        });
    }




}
