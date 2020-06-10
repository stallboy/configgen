package configgen.data;

import configgen.Logger;
import configgen.Node;
import configgen.define.AllDefine;
import configgen.define.Table;
import configgen.type.AllType;
import configgen.type.TTable;
import configgen.util.CSVParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllData extends Node {
    private final Path dataDir;
    private final Map<String, DTable> dTables = new HashMap<>();

    public AllData(Path _dataDir, String dataEncoding) {
        super(null, "AllData");
        dataDir = _dataDir;

        if (Files.isDirectory(dataDir)) {
            try {
                Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes a) {
                        String path = dataDir.relativize(file).toString();
                        if (path.endsWith(".csv")) {
                            String p = path.substring(0, path.length() - 4);
                            String configName = String.join(".", p.split("[\\\\/]")).toLowerCase();
                            List<List<String>> allLines = CSVParser.readFromFile(file, dataEncoding);
                            // Logger.mm(file.toString());
                            dTables.put(configName, new DTable(AllData.this, configName, allLines));
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public DTable getDTable(String tableName) {
        return dTables.get(tableName);
    }

    public Map<String, DTable> getDTables() {
        return dTables;
    }


    public void attachType(AllType fullType) {
        for (DTable dTable : dTables.values()) {
            dTable.setTableType(fullType.getTTable(dTable.name));
        }
    }


    public void autoFixDefine(AllDefine defineToFix, AllType firstTryType) {
        Map<String, TTable> old = new HashMap<>();
        for (TTable tTable : firstTryType.getTTables()) {
            old.put(tTable.name, tTable);
        }

        defineToFix.clearTables();
        for (DTable dTable : dTables.values()) {
            TTable ttable = old.remove(dTable.name);
            Table tableDefine;
            if (ttable != null) {
                tableDefine = ttable.getTableDefine();
                defineToFix.addTable(tableDefine);
            } else {
                tableDefine = defineToFix.newTable(dTable.name);
                Logger.verbose("new table " + tableDefine.fullName());
            }

            try {
                dTable.parse(ttable);
                dTable.autoCompleteDefine(tableDefine);
            } catch (Throwable e) {
                throw new AssertionError(dTable.name + ", 根据这个表里的数据来猜测表结构和类型出错，看是不是手动在xml里声明一下", e);
            }
        }

        old.forEach((k, cfg) -> Logger.verbose("delete table " + cfg.fullName()));
    }


}
