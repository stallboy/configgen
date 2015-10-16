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
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Generator {

    public static class Context {
        public final String arg;
        public final String type;
        public final Map<String, String> ctx = new HashMap<>();

        public Context(String arg) {
            this.arg = arg;
            String[] sp = arg.split(",");
            type = sp[0];
            for (int i = 1; i < sp.length; i++) {
                String s = sp[i];
                int c = s.indexOf(':');
                if (-1 == c)
                    ctx.put(s, null);
                else
                    ctx.put(s.substring(0, c), s.substring(c + 1));
            }
        }

        public String get(String key, String def) {
            String v = ctx.remove(key);
            return v != null ? v : def;
        }

        public boolean end() {
            if (!ctx.isEmpty()) {
                System.err.println("-gen " + type + " not support argument: " + ctx);
                return false;
            }
            return true;
        }
    }

    public static final Map<String, Generator> providers = new LinkedHashMap<>();

    static {
        new GenPack();
        new GenZip();
        new GenBin();
        new GenJava();
        new GenCs();
        new GenLua();
    }

    public static Generator create(String arg) {
        Context ctx = new Context(arg);
        Generator gen = providers.get(ctx.type);
        if (gen == null) {
            System.err.println(ctx.type + " not support");
            return null;
        }
        gen.context = ctx;
        return gen.parse(ctx) ? gen : null;
    }

    public Context context;

    public abstract String usage();

    public abstract boolean parse(Context ctx);

    public abstract void generate(Path configDir, CfgVs value) throws IOException;

    private static final Map<CfgVs, Map<String, CfgVs>> extracted = new HashMap<>();

    protected static CfgVs extract(CfgVs value, String own) {
        Map<String, CfgVs> ownMap = extracted.get(value);
        if (ownMap != null) {
            CfgVs v = ownMap.get(own);
            if (v != null)
                return v;
        }

        Logger.verbose("extract xml(" + own + ")");
        ConfigCollection ownDefine = value.type.define.extract(own);
        Cfgs ownType = new Cfgs(ownDefine);
        ownType.resolve();

        Logger.verbose("extract data(" + own + ")");
        CfgVs v = new CfgVs(ownType, value.data);
        v.verifyConstraint();

        if (ownMap == null) {
            ownMap = new HashMap<>();
            extracted.put(value, ownMap);
        }
        ownMap.put(own, v);
        return v;
    }

    protected static void mkdirs(File path) {
        if (!path.exists()) {
            if (!path.mkdirs()) {
                Logger.log("mkdirs fail: " + path.toPath().toAbsolutePath().normalize());
            }
        }
    }

    protected static void delete(File file) {
        String dir = file.isDirectory() ? "dir" : "file";
        String ok = file.delete() ? "" : " fail";
        Logger.log("delete " + dir + ok + ": " + file.toPath().toAbsolutePath().normalize());
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
