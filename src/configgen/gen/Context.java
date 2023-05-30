package configgen.gen;

import configgen.Logger;
import configgen.data.AllData;
import configgen.data.DTable;
import configgen.define.AllDefine;
import configgen.view.ViewFilter;
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

    private final AllData fullData;

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
    private ViewFilter lastViewFilter;

    private Replacement replacement;

    private final UgcDefine ugcDefine;

    Context(Path dataDir, String encoding) {
        fullDefine = new AllDefine(dataDir, encoding);
        fullData = fullDefine.readData();
        fullDefine.autoFixFullDefineByData(fullData);
        fullDefine.verifyFullDefine();

        fullType = fullDefine.resolveFullTypeAndAttachToData(fullData);
        ugcDefine = new UgcDefine(dataDir, encoding);

    }

    public Path getDataDir() {
        return fullDefine.getDataDir();
    }

    public DTable getDTable(String tableName) {
        return fullData.get(tableName);
    }

    void setI18nOrLangSwitch(String i18nFile, String langSwitchDir, String i18nEncoding, boolean crlfaslf) {
        if (i18nFile != null) {
            _isI18n = true;
            i18n = new I18n(i18nFile, i18nEncoding, crlfaslf);
        } else if (langSwitchDir != null) {
            langSwitch = new LangSwitch(langSwitchDir, i18nEncoding, crlfaslf);
        }
    }

    void setReplacement(String file) {
        replacement = new Replacement(file);
    }

    public Replacement getReplacement() {
        return replacement;
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

    public UgcDefine getUgcDefine() {
        return ugcDefine;
    }

    public AllValue makeValue(ViewFilter filter) {
        if (lastValue != null) {
            if (Objects.equals(filter, lastViewFilter)) {
                return lastValue;
            }
        }

        lastValue = null; //让它可以被尽快gc

        lastViewFilter = filter;
        lastValue = make(fullDefine.resolveType(filter));

        Logger.mm("verify " + filter.name());
        return lastValue;
    }

    private AllValue make(AllType myType) {
        AllValue value = new AllValue(myType, this);
        Logger.mm("value");
        value.verifyConstraint();
        return value;
    }
}
