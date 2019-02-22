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
    private I18n i18n = new I18n();
    private LangSwitch langSwitch = null;

    private VDb lastValue;
    private String lastValueOwn;

    Context(Path dataDir, File xmlFile, String encoding) {
        this.dataDir = dataDir;
        Logger.mm("start");
        define = new Db(xmlFile);
        Logger.mm("define");

        //define.dump(System.out);
        TDb defineType = new TDb(define);
        defineType.resolve();
        //defineType.dump(System.out);

        data = new DDb(dataDir, encoding);
        Logger.mm("data");
        data.autoCompleteDefine(define, defineType);
        define.save(xmlFile, encoding);

        type = new TDb(define);
        type.resolve();
        //type.dump(System.out);
        Logger.mm("type");
    }

    void setI18nOrLangSwitch(String i18nFile, String langSwitchDir, String i18nEncoding, boolean crlfaslf){
        if (i18nFile != null) {
            i18n = new I18n(i18nFile, i18nEncoding, crlfaslf);
        }else if (langSwitchDir != null){
            langSwitch = new LangSwitch(langSwitchDir, i18nEncoding, crlfaslf);
        }
    }

    public LangSwitch getLangSwitch(){
        return langSwitch;
    }

    public Path getDataDir() {
        return dataDir;
    }

    public I18n getI18n() {
        return i18n;
    }

    public VDb makeValue() {
        return makeValue(null);
    }

    void dump() {
        System.out.println("---define");
        define.dump(System.out);
        System.out.println("---data");
        data.dump(System.out);
        System.out.println("---type");
        type.dump(System.out);
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
