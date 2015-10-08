package configgen.gen;

import configgen.Logger;
import configgen.define.ConfigCollection;
import configgen.type.Cfgs;
import configgen.value.CfgVs;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class Generator {

    public abstract void generate(Path configDir, CfgVs value, Context ctx) throws IOException;

    public static final Map<String, Generator> providers = new HashMap<>();

    protected static CfgVs extract(CfgVs value, String own) {
        if (own == null)
            return value;

        Logger.verbose("extract xml(" + own + ")");
        ConfigCollection ownDefine = value.type.define.extract(own);
        Cfgs ownType = new Cfgs(ownDefine);
        ownType.resolve();

        Logger.verbose("extract data(" + own + ")");
        CfgVs v = new CfgVs(ownType, value.data);
        v.verifyConstraint();
        return v;
    }

    protected static void mkdirs(File path) {
        if (!path.exists()) {
            if (!path.mkdirs()) {
                Logger.log("mkdirs fail: " + path);
            }
        }
    }

    protected static void delete(File file) {
        if (!file.delete()) {
            Logger.log("delete file fail: " + file);
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
