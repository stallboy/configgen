package configgen.type;

import configgen.Node;
import configgen.define.Ref;

public class MRef extends Node {
    public final Ref define;
    public Cfg ref;

    public MRef(TBean parent, Ref r) {
        super(parent, r.name);
        define = r;
    }

    public void resolve() {
        ref = ((Cfgs) root).cfgs.get(define.ref);
        require(ref != null, "ref not found", define.fullName());
    }
}
