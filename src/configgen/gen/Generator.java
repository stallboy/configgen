package configgen.gen;

import configgen.Utils;
import configgen.define.ConfigCollection;
import configgen.type.Cfgs;
import configgen.value.CfgVs;

import java.io.IOException;
import java.nio.file.Path;

public abstract class Generator {
    protected Path configDir;
    protected CfgVs value;
    protected Context ctx;

    public Generator(Path dir, CfgVs value, Context ctx) {
        this.configDir = dir;
        this.value = value;
        this.ctx = ctx;
    }

    public abstract void gen() throws IOException;

    protected void applyOwn(String own) {
        if (own == null)
            return;
        Utils.verbose("extract define(" + own + ")");
        ConfigCollection ownDefine = value.type.define.extract(own);
        Utils.verbose("resolve to type(" + own + ")");
        Cfgs ownType = new Cfgs(ownDefine);
        ownType.resolve();

        Utils.verbose("construct and verify value(" + own + ")");
        value = new CfgVs(ownType, value.data);
        value.verifyConstraint();
    }
}
