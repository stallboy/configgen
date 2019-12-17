package configgen.gen;

import configgen.Logger;
import configgen.data.AllData;
import configgen.define.AllDefine;
import configgen.type.AllType;
import configgen.value.AllValue;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class Context {
    private final AllData fullData;
    //不存储fullValue

    private boolean _isI18n = false;
    private I18n i18n = new I18n();
    private LangSwitch langSwitch = null;

    private AllValue lastValue;
    private String lastValueOwn;

    Context(Path dataDir, File xmlFile, String encoding) {
        fullData = new AllData(dataDir, encoding);
        fullData.refineDefineAndType(xmlFile, encoding);
    }

    public Path getDataDir() {
        return fullData.getDataDir();
    }

    public AllData getFullData() {
        return fullData;
    }

    void setI18nOrLangSwitch(String i18nFile, String langSwitchDir, String i18nEncoding, boolean crlfaslf) {
        if (i18nFile != null) {
            _isI18n = true;
            i18n = new I18n(i18nFile, i18nEncoding, crlfaslf);
        } else if (langSwitchDir != null) {
            langSwitch = new LangSwitch(langSwitchDir, i18nEncoding, crlfaslf);
        }
    }

    void dump() {
        fullData.getFullDefine().dump(System.out);
        fullData.getFullType().dump(System.out);
    }

    public LangSwitch getLangSwitch() {
        return langSwitch;
    }

    public boolean isI18n() {
        return _isI18n;
    }

    public I18n getI18n() {
        return i18n;
    }

    public AllValue makeValue() {
        return makeValue(null);
    }


    public AllValue makeValue(String own) {
        if (lastValue != null) {
            if (Objects.equals(own, lastValueOwn)) {
                return lastValue;
            }
        }

        lastValue = null; //让它可以被尽快gc
        AllValue value;
        if (own == null || own.isEmpty()) {
            value = make(fullData.getFullType());
        } else {
            AllDefine ownDefine = fullData.getFullDefine().extract(own);
            AllType ownType = new AllType(ownDefine);
            ownType.resolve();
            value = make(ownType);
        }

        lastValueOwn = own;
        lastValue = value;

        Logger.mm("verify " + (own == null ? "" : own));
        return value;
    }

    private AllValue make(AllType myType) {
        AllValue value = new AllValue(myType, fullData, this);
        Logger.mm("value");
        value.verifyConstraint();
        return value;
    }
}
