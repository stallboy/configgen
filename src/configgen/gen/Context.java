package configgen.gen;

import configgen.Logger;
import configgen.data.DDb;
import configgen.define.Db;
import configgen.type.TDb;
import configgen.value.I18n;
import configgen.value.VDb;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class Context {
    private final Path dataDir;
    private final Db define;
    private final TDb type;
    private final DDb data;
    private final I18n i18n;

    private VDb lastValue;
    private String lastValueOwn;

    Context(Path dataDir, File xmlFile, String encoding, String i18nFile, String i18nEncoding, boolean crlfaslf) {
        this.dataDir = dataDir;
        Logger.mm("start");
        define = new Db(xmlFile);
        Logger.mm("define");

        //define.dump(System.out);
        TDb defineType = new TDb(define);
        defineType.resolve();
        //type.dump(System.out);

        data = new DDb(dataDir, encoding);
        Logger.mm("data");
        data.autoCompleteDefine(defineType);
        define.save(xmlFile, encoding);

        type = new TDb(define);
        type.resolve();
        Logger.mm("type");

        i18n = new I18n(i18nFile, i18nEncoding, crlfaslf);
    }

    public Path getDataDir() {
        return dataDir;
    }

    public VDb makeValue() {
        return makeValue(null);
    }

    public VDb makeValue(String own) {
        if (lastValue != null) {
            if (Objects.equals(own, lastValueOwn)) {
                return lastValue;
            }
        }

        lastValue = null; //make it gc able
        VDb value;
        if (own == null || own.isEmpty()) {
            value = make(type);
        } else {
            Db ownDefine = define.extract(own);
            TDb ownType = new TDb(ownDefine);
            ownType.resolve();
            value = make(ownType);
        }

        lastValueOwn = own;
        lastValue = value;

        Logger.mm("verify " + (own == null ? "" : own));
        return value;
    }

    private VDb make(TDb myType) {
        VDb value = new VDb(myType, data, i18n);
        Logger.mm("value");
        value.verifyConstraint();
        return value;
    }
}
