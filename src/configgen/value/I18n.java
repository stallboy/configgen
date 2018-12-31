package configgen.value;

import configgen.util.CSV;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class I18n {
    private Map<String, Map<String, String>> map = null;
    private Map<String, String> curTable = null;
    private boolean isCRLFAsLF;

    public I18n(String file, String encoding, boolean crlfaslf) {
        if (file == null) {
            return;
        }
        map = new HashMap<>();
        List<List<String>> rows = CSV.readFromFile(Paths.get(file), encoding);
        List<String> row0 = rows.get(0);
        if (row0 == null) {
            throw new IllegalArgumentException("国际化i18n文件为空");
        }
        if (row0.size() != 3) {
            throw new IllegalArgumentException("国际化i18n文件列数不为3");
        }

        isCRLFAsLF = crlfaslf;
        for (List<String> row : rows) {
            if (CSV.isEmptyRecord(row)) {
                continue;
            }
            if (row.size() != 3) {
                System.out.println(row + " 不是3列，被忽略");
            } else {
                String table = row.get(0);
                String raw = row.get(1);
                String i18 = row.get(2);
                raw = normalizeRaw(raw);

                Map<String, String> m = map.computeIfAbsent(table, k -> new HashMap<>());
                m.put(raw, i18);
            }
        }
    }

    private String normalizeRaw(String raw) {
        if (isCRLFAsLF) {
            return raw.replaceAll("\r\n", "\n");
        } else {
            return raw;
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

        if (raw == null) {
            return null;
        }

        raw = normalizeRaw(raw);
        String text = curTable.get(raw);
        if (text == null || text.isEmpty()) {
            return null;
        }
        return text;
    }

}
