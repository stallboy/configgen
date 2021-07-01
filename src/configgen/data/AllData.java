package configgen.data;

import configgen.Logger;
import configgen.Node;
import configgen.define.AllDefine;
import configgen.define.Table;
import configgen.type.AllType;
import configgen.type.TTable;

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
import java.util.Set;

public class AllData extends Node {
    private final Map<String, DTable> dTables = new HashMap<>();

    public AllData(Path dataDir, String dataEncoding) {
        super(null, "AllData");

        if (Files.isDirectory(dataDir)) {
            try {
                Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes a) {
                        File file = path.toFile();
                        EFileFormat fileFormat = DataFormatUtils.getFileFormat(file);
                        if (fileFormat != EFileFormat.NONE) {
                            String pathName = dataDir.relativize(path).toString();
                            String pathWithoutExtension = pathName;
                            int i = pathName.lastIndexOf('.');
                            if (i >= 0) {
                                pathWithoutExtension = pathName.substring(0, i);
                            }
                            String configName = String.join(".", pathWithoutExtension.split("[\\\\/]")).toLowerCase();
                            List<List<String>> allLines = DataFormatUtils.readFromFile(file, dataEncoding);
                            // Logger.mm(file.toString());
                            dTables.put(configName, new DTable(AllData.this, configName, allLines, fileFormat));
                        }

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
        Set<String> currentRemains = defineToFix.getTableNames();
        for (DTable dTable : dTables.values()) {
            TTable currentTableType = firstTryType.getTTable(dTable.name);
            boolean contains = currentRemains.remove(dTable.name);

            Table tableToFix;
            if (contains) {
                tableToFix = currentTableType.getTableDefine();
            } else {
                tableToFix = defineToFix.newTable(dTable.name);
                Logger.verbose("new table " + tableToFix.fullName());
            }

            try {
                dTable.autoFixDefine(tableToFix, currentTableType);
            } catch (Throwable e) {
                throw new AssertionError(dTable.name + ", 根据这个表里的数据来猜测表结构和类型出错，看是不是手动在xml里声明一下", e);
            }
        }

        for (String currentRemain : currentRemains) {
            defineToFix.removeTable(currentRemain);
            Logger.verbose("delete table " + defineToFix.fullName() + "." + currentRemain);
        }
    }


}
