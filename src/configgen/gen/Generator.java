package configgen.gen;

import configgen.Logger;
import configgen.define.ConfigCollection;
import configgen.type.Cfgs;
import configgen.value.CfgVs;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

public abstract class Generator {
    protected final Path configDir;
    protected CfgVs value;
    protected final Context ctx;

    public Generator(Path dir, CfgVs value, Context ctx) {
        this.configDir = dir;
        this.value = value;
        this.ctx = ctx;
    }

    public abstract void gen() throws IOException;

    protected void applyOwn(String own) {
        if (own == null)
            return;
        Logger.verbose("extract define(" + own + ")");
        ConfigCollection ownDefine = value.type.define.extract(own);
        Logger.verbose("resolve to type(" + own + ")");
        Cfgs ownType = new Cfgs(ownDefine);
        ownType.resolve();

        Logger.verbose("construct and verify value(" + own + ")");
        value = new CfgVs(ownType, value.data);
        value.verifyConstraint();
    }

    protected static void mkdirs(File path) {
        if (!path.exists()) {
            if (!path.mkdirs()) {
                Logger.log("mkdirs fail: " + path);
            }
        }
    }

    protected static String upper1(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    protected static String upper1Only(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    protected static String lower1(String value) {
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    protected static PrintStream cachedPrintStream(File file, String encoding) throws IOException {
        return new PrintStream(new CachedFileOutputStream(file), false, encoding);
    }
}
