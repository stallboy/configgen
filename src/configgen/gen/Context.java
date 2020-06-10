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
    private final AllDefine fullDefine;
    //不存储fullValue

    private boolean _isI18n = false;
    private I18n i18n = new I18n();
    private LangSwitch langSwitch = null;

    private AllValue lastValue;
    private String lastValueOwn;

    Context(Path xmlPath, String encoding) {
        fullDefine = new AllDefine(xmlPath, encoding);
        fullDefine.readDataFilesThenAutoFix();
    }

    public Path getDataDir() {
        return fullDefine.getDataDir();
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
        fullDefine.dump(System.out);
        fullDefine.getFullType().dump(System.out);
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
            value = make(fullDefine.getFullType());
        } else {
            AllDefine ownDefine = fullDefine.extractOwn(own);
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
        AllValue value = new AllValue(myType, fullDefine, this);
        Logger.mm("value");
        value.verifyConstraint();
        return value;
    }
}
