package configgen.gen;

import configgen.value.I18n;

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
        try {
            Files.list(Paths.get(path)).forEach(langFilePath -> {
                String langFileName = langFilePath.getFileName().toString();
                if (langFileName.endsWith(".csv")) {
                    String lang = langFileName.substring(0, langFileName.length() - 4);
                    langMap.put(lang, new Lang(lang, new I18n(langFilePath, encoding, crlfaslf)));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        langMap.put("zh-cn", new Lang("zh-cn", new I18n())); //原始csv里是中文
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
