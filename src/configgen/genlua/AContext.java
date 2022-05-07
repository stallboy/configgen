package configgen.genlua;

import configgen.gen.LangSwitch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class AContext {
    private static final AContext instance = new AContext();

    static AContext getInstance() {
        return instance;
    }

    private String pkgPrefixStr;
    private LangSwitch langSwitch;
    private boolean sharedEmptyTable;
    private boolean shared;
    private boolean packBool;
    private boolean tryColumnMode;
    private boolean noStr; //只用于测试

    private String emptyTableStr;
    private String listMapPrefixStr;
    private String listMapPostfixStr;

    private final Set<String> forbidLocalNames = new HashSet<>(Arrays.asList("Beans", "this", "mk",
                                                                             "A", //表示共享Table
                                                                             "E", //表示emptyTable
                                                                             "R"  //表示为共享Table的一个包装方法 --> 后改为list，map的封装，用于检测修改
    ));

    private AStat statistics;

    void init(String pkg, LangSwitch ls, boolean shareEmptyTable, boolean share,
              boolean col, boolean packBool, boolean noStr, boolean rForOldShared) {
        langSwitch = ls;
        sharedEmptyTable = shareEmptyTable;
        shared = share;
        tryColumnMode = col;
        this.packBool = packBool;
        this.noStr = noStr;

        if (sharedEmptyTable) {
            emptyTableStr = "E";
        } else {
            emptyTableStr = "{}";
        }

        if (rForOldShared) {
            listMapPrefixStr = "{";
            listMapPostfixStr = "}";
        } else {
            listMapPrefixStr = "R({";
            listMapPostfixStr = "})";
        }

        if (pkg.length() == 0) {
            pkgPrefixStr = "";
        } else {
            pkgPrefixStr = pkg + ".";
            forbidLocalNames.add(pkg);
        }

        statistics = new AStat();
    }


    boolean isForbidName(String name) {
        return forbidLocalNames.contains(name);
    }

    LangSwitch getLangSwitch() {
        return langSwitch;
    }

    boolean isShared() {
        return shared;
    }

    boolean isPackBool() {
        return packBool;
    }

    boolean isTryColumnMode() {
        return tryColumnMode;
    }

    boolean isNoStr() {
        return noStr;
    }


    String getEmptyTableStr() {
        return emptyTableStr;
    }

    String getPkgPrefixStr() {
        return pkgPrefixStr;
    }


    public String getListMapPrefixStr() {
        return listMapPrefixStr;
    }

    public String getListMapPostfixStr() {
        return listMapPostfixStr;
    }


    AStat getStatistics() {
        return statistics;
    }

}
