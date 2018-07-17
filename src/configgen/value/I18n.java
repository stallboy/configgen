package configgen.value;

import configgen.data.CSV;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class I18n {
    private Map<String, String> map = null;

    public I18n(String file) {
        if (file == null) {
            return;
        }
        map = new HashMap<>();
        List<List<String>> rows = CSV.readFromFile(new File(file), StandardCharsets.UTF_8);
        List<String> row0 = rows.get(0);
        if (row0 == null) {
            throw new IllegalArgumentException("国际化i18n文件为空");
        }
        if (row0.size() != 2) {
            throw new IllegalArgumentException("国际化i18n文件列数不为2");
        }

        for (List<String> row : rows) {
            String raw = row.get(0);
            String i18 = row.get(1);
            map.put(raw, i18);
        }
    }

    public Map<String, String> getAll() {
        return map;
    }

    public String get(String raw) {
        if (map == null) {
            return raw;
        }

        String text = map.get(raw);
        if (text == null || text.isEmpty()) {
            if (!raw.isEmpty()){
                System.out.println(raw + " 未翻译");
            }
            return raw;
        }
        return text;
    }

}
