package configgen.gen;

import configgen.Logger;
import configgen.data.DDb;
import configgen.define.Db;
import configgen.type.TDb;
import configgen.value.I18n;
import configgen.value.VDb;

import java.nio.file.Path;
import java.io.File;
import java.util.Objects;

public class Context {
    public final Db define;
    public final TDb type;
    public final DDb data;
    private final I18n i18n;

    Context(Path dataDir, File xmlFile, String encoding, String i18nFile, String i18nEncoding, boolean crlfaslf) {
        mm("start");
        define = new Db(xmlFile);
        mm("define");

        //define.dump(System.out);
        TDb defineType = new TDb(define);
        defineType.resolve();
        mm("defineType");
        //type.dump(System.out);

        data = new DDb(dataDir, encoding);
        data.autoCompleteDefine(defineType);
        define.save(xmlFile, encoding);
        mm("data");

        type = new TDb(define);
        type.resolve();
        mm("type");

        i18n = new I18n(i18nFile, i18nEncoding, crlfaslf);
    }

    private static void mm(String step) {
        //Runtime.getRuntime().gc();
        Logger.printf("%s\t use %dm, total %dm\n", step, Runtime.getRuntime().totalMemory() / 1024 / 1024, Runtime.getRuntime().maxMemory() / 1024 / 1024);
    }

    private VDb lastValue;
    private String lastValueOwn;

    void verify() {
        VDb value = new VDb(type, data, i18n);
        value.verifyConstraint();
    }

    public VDb makeValue() {
        return makeValue(null);
    }

    public VDb makeValue(String own) {
        if (lastValue != null) {
            if (Objects.equals(own, lastValueOwn)) {
                return lastValue;
            }
            lastValue = null;
        }

        VDb value;
        if (own == null || own.isEmpty()) {
            value = new VDb(type, data, i18n);
            value.verifyConstraint();
        } else {
            Db ownDefine = define.extract(own);
            TDb ownType = new TDb(ownDefine);
            ownType.resolve();

            value = new VDb(ownType, data, i18n);
            value.verifyConstraint();
        }

        lastValueOwn = own;
        lastValue = value;

        mm("mk " + own);
        return value;
    }

}
