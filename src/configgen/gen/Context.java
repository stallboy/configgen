package configgen.gen;

import configgen.Logger;
import configgen.define.AllDefine;
import configgen.type.AllType;
import configgen.value.AllValue;

import java.nio.file.Path;
import java.util.Objects;

public class Context {
    // fullValue很费内存，在用到时再生成，而fullData在fullDefine里
    private final AllDefine fullDefine;
    private final AllType fullType;

    private boolean _isI18n = false;
    private I18n i18n = new I18n();        // 这个是国际化,直接改成对应国家语言
    private LangSwitch langSwitch = null;  // 这个是要实现客户端可在多国语言间切换语言


    Context(Path xmlPath, String encoding) {
        fullDefine = new AllDefine(xmlPath, encoding);
        fullType = fullDefine.readData_AutoFix_ResolveType();
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
        fullType.dump(System.out);
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


    private AllValue lastValue;
    private String lastValueOwn;

    public AllValue makeValue(String own) {
        if (lastValue != null) {
            if (Objects.equals(own, lastValueOwn)) {
                return lastValue;
            }
        }

        lastValue = null; //让它可以被尽快gc
        AllValue value;
        if (own == null || own.isEmpty()) {
            value = make(fullType);
        } else {
            value = make(fullDefine.resolvePartType(own));
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
