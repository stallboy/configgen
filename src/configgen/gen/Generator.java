package configgen.gen;

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
}
