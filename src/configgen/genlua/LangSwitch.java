package configgen.genlua;

import configgen.value.I18n;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LangSwitch {
    static class Lang {
        I18n i18n;
        List<String> idToStr = new ArrayList<>();

        Lang(Path p) {
            i18n = new I18n(p, "UTF-8", true);
            idToStr.add(""); //这个是第一个，重用
        }

        Lang() {
            i18n = new I18n();
            idToStr.add("");
        }
    }

    static Map<String, Lang> langMap = new TreeMap<>();
    private static int next;

    public static void setLangI18nDir(Path path) throws IOException {
        Files.list(path).forEach(langFilePath -> {
            String langFileName = langFilePath.getFileName().toString();
            String lang = langFileName.substring(0, langFileName.length() - 4);
            langMap.put(lang, new Lang(langFilePath));
        });

        langMap.put("zh-cn", new Lang());
    }

    public static void enterTable(String tableName) {
        for (Lang lang : langMap.values()) {
            lang.i18n.enterTable(tableName);
        }
    }

    public static int enterText(String raw) {
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

}
