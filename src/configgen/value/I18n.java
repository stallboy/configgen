package configgen.value;

import configgen.util.CSV;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class I18n {
    private Map<String, Map<String, String>> map = null;
    private Map<String, String> curTable = null;

    public I18n(String file, String encoding, boolean crlfaslf) {
        if (file == null) {
            return;
        }
        map = new HashMap<>();
        List<List<String>> rows = CSV.readFromFile(new File(file), encoding, true);
        List<String> row0 = rows.get(0);
        if (row0 == null) {
            throw new IllegalArgumentException("国际化i18n文件为空");
        }
        if (row0.size() != 3) {
            throw new IllegalArgumentException("国际化i18n文件列数不为3");
        }

        for (List<String> row : rows) {
            if (row.size() != 3) {
                System.out.println(row + " 不是3列，被忽略");
            } else {
                String table = row.get(0);
                String raw = row.get(1);
                String i18 = row.get(2);
                if (crlfaslf) {
                    raw = raw.replaceAll("\r\n", "\n");
                    i18 = i18.replaceAll("\r\n", "\n");
                }
                Map<String, String> m = map.computeIfAbsent(table, k -> new HashMap<>());
                m.put(raw, i18);
            }
        }
    }

    public void enter(String table) {
        if (map == null) {
            return;
        }
        curTable = map.get(table);
    }

    public String get(String raw) {
        if (curTable == null) {
            return null;
        }

        String text = curTable.get(raw);
        if (text == null || text.isEmpty()) {
            return null;
        }
        return text;
    }

}
