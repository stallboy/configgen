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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllData extends Node {
    private final Path dataDir;
    private final Map<String, DTable> dTables = new HashMap<>();

    private AllDefine fullDefine;
    private AllType fullType;


    public AllData(Path _dataDir, String dataEncoding) {
        super(null, "AllData");
        dataDir = _dataDir;

        try {
            Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes a) {
                    String path = dataDir.relativize(file).toString();
                    if (path.endsWith(".csv")) {
                        String p = path.substring(0, path.length() - 4);
                        String configName = String.join(".", p.split("[\\\\/]")).toLowerCase();
                        List<List<String>> allLines = CSVParser.readFromFile(file, dataEncoding);
//                        Logger.mm(file.toString());
                        dTables.put(configName, new DTable(AllData.this, configName, allLines));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getDataDir() {
        return dataDir;
    }

    public DTable getDTable(String tableName) {
        return dTables.get(tableName);
    }

    public AllDefine getFullDefine() {
        return fullDefine;
    }

    public AllType getFullType() {
        return fullType;
    }


    public void refineDefineAndType(File xmlFile, String encoding) {
        fullDefine = new AllDefine(xmlFile);
        Logger.mm("define");

        AllType firstTryType = new AllType(fullDefine);
        firstTryType.resolve();

        autoCompleteDefine(fullDefine, firstTryType);
        fullDefine.save(xmlFile, encoding);

        fullType = new AllType(fullDefine);
        fullType.resolve();
        Logger.mm("type");

        for (DTable table : dTables.values()) {
            table.setTableType(fullType.getTTable(table.name));
        }
    }

    private void autoCompleteDefine(AllDefine allDefine, AllType allType) {
        Map<String, TTable> old = new HashMap<>();
        for (TTable tTable : allType.getTTables()) {
            old.put(tTable.name, tTable);
        }

        allDefine.tables.clear();
        for (DTable dTable : dTables.values()) {
            TTable ttable = old.remove(dTable.name);
            Table tableDefine;
            if (ttable != null) {
                tableDefine = ttable.getTableDefine();
                allDefine.tables.put(dTable.name, tableDefine);
            } else {
                tableDefine = allDefine.newTable(dTable.name);
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
