package configgen.type;

import configgen.Node;

public class ListRef extends Node {
    public final String[] keys;
    public Cfg ref;
    public final String[] refKeys;

    private final String refStr;

    public ListRef(TBean parent, configgen.define.ListRef define) {
        this(parent, define.name, define.keys, define.ref, define.refKeys);
    }

    public ListRef(TBean parent, String key, String ref, String refField) {
        this(parent, key, new String[]{key}, ref, new String[]{refField});
    }

    private ListRef(TBean parent, String name, String[] keys, String ref, String[] refKeys) {
        super(parent, name);
        this.keys = keys;
        this.refStr = ref;
        this.refKeys = refKeys;
    }

    public void resolve() {
        ref = ((Cfgs) root).cfgs.get(refStr);
        require(ref != null, "ref not found", refStr);

        for (String key : keys) {
            require(null != ((TBean) parent).define.fields.get(key), "key not exist", key);
        }

        for (String rk : refKeys) {
            require(null != ref.tbean.define.fields.get(rk), "ref key not exist", rk);
        }
    }
}
