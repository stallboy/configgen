package configgen.gen;

import configgen.Logger;
import configgen.data.DTable;
import configgen.define.AllDefine;
import configgen.type.AllType;
import configgen.value.AllValue;

import java.nio.file.Path;
import java.util.Objects;


public class Context {
    /**
     * 完整的结构定义，xml <-> object
     * 注意fullDefine中，包含了完整的fullData，方便根据csv头的信息来自动修复AutoFix xml。
     * 这里假设 工作流是策划修改csv来改变配置结构，而基本不用手工修改xml，xml自动匹配csv，
     * 程序如果发现不合适，再手工修改xml。
     */
    private final AllDefine fullDefine;
    /**
     * 完整的包含类型的结构定义，xml中的type被解析，ref被关联
     */
    private final AllType fullType;


    /**
     * 直接国际化,直接改成对应国家语言
     */
    private boolean _isI18n = false;
    private I18n i18n = new I18n();

    /**
     * 这个是要实现客户端可在多国语言间切换语言，所以客户端服务器都需要完整的多国语言信息，而不能如i18n那样直接替换
     */
    private LangSwitch langSwitch = null;


    /**
     * 优化，避免gen多次时，重复生成value
     * 注意这里不再立马生成fullValue，因为很费内存，在用到时再生成。
     */
    private AllValue lastValue;
    private String lastValueOwn;


    Context(Path xmlPath, String encoding) {
        fullDefine = new AllDefine(xmlPath, encoding);
        fullType = fullDefine.readData_AutoFix_ResolveType();
    }

    public Path getDataDir() {
        return fullDefine.getDataDir();
    }

    public DTable getDTable(String tableName) {
        return fullDefine.getDTable(tableName);
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
        AllValue value = new AllValue(myType, this);
        Logger.mm("value");
        value.verifyConstraint();
        return value;
    }
}
