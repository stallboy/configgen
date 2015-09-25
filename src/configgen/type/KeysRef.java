package configgen.type;

import configgen.Node;
import configgen.define.Ref;

public class KeysRef extends Node {
    public final Ref define;
    public Cfg ref;
    public Cfg nullableRef;

    public KeysRef(TBean parent, Ref r) {
        super(parent, r.name);
        define = r;
    }

    void resolve() {
        ref = ((Cfgs) root).cfgs.get(define.ref);
        nullableRef = ((Cfgs) root).cfgs.get(define.nullableRef);
        int c = 0;
        if (ref != null)
            c++;
        if (nullableRef != null)
            c++;
        define.Assert(c == 1, "ref count=" + c);
    }
}
