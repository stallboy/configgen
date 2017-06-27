package configgen.gen;

import configgen.Logger;
import configgen.define.Db;
import configgen.genjava.GenJavaData;
import configgen.genjava.GenJavaCode;
import configgen.type.TDb;
import configgen.value.VDb;

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
        GenJavaCode.register();
        GenCs.register();
        GenLua.register();
        GenAllRefValues.register();

        GenJavaData.register();
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

    public abstract void generate(VDb value) throws IOException;

    protected void require(boolean cond, String... str) {
        if (!cond)
            throw new AssertionError(getClass().getSimpleName() + ": " + String.join(",", str));
    }

    private static final Map<VDb, Map<String, VDb>> extracted = new HashMap<>();

    protected static VDb extract(VDb value, String own) {
        Map<String, VDb> ownMap = extracted.get(value);
        if (ownMap != null) {
            VDb v = ownMap.get(own);
            if (v != null)
                return v;
        }

        Logger.verbose("extract xml(" + own + ")");
        Db ownDefine = value.dbType.dbDefine.extract(own);
        //ownDefine.dump(System.out);
        TDb ownType = new TDb(ownDefine);
        ownType.resolve();

        Logger.verbose("extract data(" + own + ")");
        VDb v = new VDb(ownType, value.dbData);
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

    protected static String
    lower1(String value) {
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }
}
