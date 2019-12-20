package configgen.genlua;

import configgen.value.*;

class Ctx {
    private VTable vTable;
    private CtxColumnStore ctxColumnStore;
    private CtxShared ctxShared;
    private CtxName ctxName;


    Ctx(VTable vtable) {
        vTable = vtable;
        ctxName = new CtxName();
        ctxShared = new CtxShared();
        ctxColumnStore = new CtxColumnStore();
    }

    VTable getVTable() {
        return vTable;
    }

    CtxName getCtxName() {
        return ctxName;
    }

    CtxShared getCtxShared() {
        return ctxShared;
    }

    CtxColumnStore getCtxColumnStore() {
        return ctxColumnStore;
    }

    void parseShared() {
        ctxShared.parseShared(this);
    }

    void parseColumnStore() {
        ctxColumnStore.parseColumnStore(this);
    }

}
