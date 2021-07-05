package configgen.gen;

import configgen.util.EFileFormat;
import configgen.util.SheetUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class LangSwitch {

    public static class Lang {
        private String lang;
        private I18n i18n;
        private List<String> idToStr = new ArrayList<>();

        Lang(String lang, I18n i18n) {
            this.lang = lang;
            this.i18n = i18n;
            idToStr.add(""); //这个是第一个，重用
        }

        public String getLang() {
            return lang;
        }

        public List<String> getStrList() {
            return idToStr;
        }
    }


    private Map<String, Lang> langMap = new TreeMap<>();
    private int next;
    private String[] tmp;
    private String[] tmpEmpty;


    LangSwitch(String path, String encoding, boolean crlfaslf) {
        langMap.put("zh_cn", new Lang("zh_cn", new I18n())); //原始csv里是中文
        try {
            Files.list(Paths.get(path)).forEach(langFilePath -> {
                EFileFormat format = SheetUtils.getFileFormat(langFilePath.toFile());
                if (format != null) {
                    String langName = langFilePath.getFileName().toString();
                    int i = langName.lastIndexOf(".");
                    if (i >= 0) {
                        langName = langName.substring(0, i);
                    }
                    langMap.put(langName, new Lang(langName, new I18n(langFilePath, encoding, crlfaslf)));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        int langCnt = langMap.size();
        tmp = new String[langCnt];

        tmpEmpty = new String[langCnt];
        for (int i = 0; i < langCnt; i++) {
            tmpEmpty[i] = "";
        }
    }


    public void enterTable(String tableName) {
        for (Lang lang : langMap.values()) {
            lang.i18n.enterTable(tableName);
        }
    }

    public int enterText(String raw) {
        if (raw.isEmpty()) {
            return 0;
        }

        for (Lang lang : langMap.values()) {
            String t = lang.i18n.enterText(raw);
            if (t == null) {
                t = raw;
            }
            lang.idToStr.add(t);
        }
        next++;
        return next;
    }

    public Collection<Lang> getAllLangInfo() {
        return langMap.values();
    }

    public String[] findAllLangText(String raw) {
        if (raw.isEmpty()) {
            return tmpEmpty;
        }

        int i = 0;
        for (Lang lang : langMap.values()) {
            String t = lang.i18n.enterText(raw);
            if (t == null) {
                t = raw;
            }
            tmp[i++] = t;
        }
        return tmp;
    }


}
