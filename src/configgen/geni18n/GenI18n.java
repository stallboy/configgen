package configgen.geni18n;

import configgen.gen.*;
import configgen.util.CSVWriter;
import configgen.gen.I18n;

import java.io.File;
import java.util.*;

public final class GenI18n extends Generator implements I18n.Collector {
    public static void register() {
        Generators.addProvider("i18n", new GeneratorProvider() {
            @Override
            public Generator create(Parameter parameter) {
                return new GenI18n(parameter);
            }

            @Override
            public String usage() {
                return "file:../i18n/i18n-config.csv,encoding:GBK";
            }
        });
    }

    private File file;
    private String encoding;

    private GenI18n(Parameter parameter) {
        super(parameter);
        file = new File(parameter.get("file", "../i18n/i18n-config.csv"));
        encoding = parameter.get("encoding", "GBK");
        parameter.end();
    }

    @Override
    public void generate(Context ctx) {
        table2TextMap.clear();
        ctx.getI18n().setCollector(this);
        ctx.makeValue();

        List<List<String>> rows = new ArrayList<>(64 * 1024);
        for (Map.Entry<String, Map<String, String>> table2TextEntry : table2TextMap.entrySet()) {
            String table = table2TextEntry.getKey();
            for (Map.Entry<String, String> s : table2TextEntry.getValue().entrySet()) {
                List<String> r = new ArrayList<>(2);
                r.add(table);
                r.add(s.getKey());
                r.add(s.getValue());
                rows.add(r);
            }
        }

        CSVWriter.writeToFile(file, encoding, rows);
    }

    private Map<String, Map<String, String>> table2TextMap = new TreeMap<>();
    private String lastTable;
    private Map<String, String> lastTextMap;

    @Override
    public void enterTable(String table) {
        lastTable = table;
        lastTextMap = null;
    }

    @Override
    public void enterText(String original, String text) {
        if (lastTextMap == null) {
            lastTextMap = new LinkedHashMap<>();
            table2TextMap.put(lastTable, lastTextMap);
        }
        lastTextMap.put(original, text);
    }
}
