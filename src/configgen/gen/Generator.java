package configgen.gen;

import configgen.Logger;
import configgen.define.ConfigCollection;
import configgen.type.Cfgs;
import configgen.value.CfgVs;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;

public abstract class Generator {

    public static final Map<String, Provider> providers = new LinkedHashMap<>();

    static {
        GenPack.register();
        GenJava.register();
        GenCs.register();
        GenLua.register();
    }

    public static Generator create(String arg) {
        Parameter parameter = new Parameter(arg);
        Provider provider = providers.get(parameter.type);
        if (provider == null) {
            System.err.println(parameter.type + " not support");
            return null;
        }
        return provider.create(parameter);
    }

    public final Parameter parameter;

    protected Generator(Parameter parameter) {
        this.parameter = parameter;
    }

    public abstract void generate(CfgVs value) throws IOException;

    protected void require(boolean cond, String... str) {
        if (!cond)
            throw new AssertionError(getClass().getSimpleName() + ": " + String.join(",", str));
    }

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

    protected static TabPrintStream createSource(File file, String encoding) throws IOException {
        return new TabPrintStream(new PrintStream(new CachedFileOutputStream(file), false, encoding));
    }

    protected static ZipOutputStream createZip(File file) throws IOException {
        return new ZipOutputStream(new CheckedOutputStream(new CachedFileOutputStream(file), new CRC32()));
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
}
