package configgen.type;

import configgen.Node;

public class ListRef extends Node {
    public final configgen.define.ListRef define;
    public Cfg ref;

    public ListRef(TBean parent, configgen.define.ListRef define) {
        super(parent, define.name);
        this.define = define;
    }

    public ListRef copy(TBean parent) {
        ListRef c = new ListRef(parent, define);
        c.ref = ref;
        return c;
    }

    void resolve(){
        ref = ((Cfgs)root).cfgs.get(define.ref);
        define.Assert(ref != null, "ref not found", define.ref);

        for (String key : define.keys) {
            define.Assert( null != ((TBean)parent).define.fields.get(key), "key not exist", key);
        }

        for (String rk : define.refFields) {
            define.Assert(null != ref.tbean.fields.get(rk), "ref key not exist", rk);
        }
    }
}
