package configgen.data;

import configgen.Logger;
import configgen.Node;
import configgen.define.AllDefine;
import configgen.define.Table;
import configgen.type.AllType;
import configgen.type.TTable;
import configgen.util.EFileFormat;
import configgen.util.SheetData;
import configgen.util.SheetUtils;
import configgen.view.ViewFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class AllData extends Node {
    private final Map<String, DTable> dTables = new HashMap<>();

    public AllData(AllDefine allDefine) {
        super(null, "AllData");

        Path dataDir = allDefine.getDataDir();
        if (!Files.isDirectory(dataDir)) {
            throw new IllegalArgumentException("配置顶级目录必须是目录. dataDir = " + dataDir);
        }

        Map<String, List<DSheet>> dSheetMap = new TreeMap<>();
        try {
            Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes a) {
                    List<SheetData> sheetDataList =
                            SheetUtils.readFromFile(path.toFile(), allDefine.getEncoding(), AllData::acceptSheet);

                    for (SheetData sheetData : sheetDataList) {
                        DSheet sheet = DSheet.create(dataDir, AllData.this, sheetData);
                        List<DSheet> sheetList = dSheetMap.computeIfAbsent(sheet.getConfigName(), k -> new ArrayList<>());
                        sheetList.add(sheet);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<String, List<DSheet>> e : dSheetMap.entrySet()) {
            String configName = e.getKey();
            List<DSheet> sheetList = e.getValue();

            DTable dTable = new DTable(AllData.this, configName, sheetList);
            dTables.put(configName, dTable);
        }
    }

    public DTable get(String table) {
        return dTables.get(table);
    }


    public void attachType(AllType fullType) {
        for (DTable dTable : dTables.values()) {
            dTable.setTableType(fullType.getTTable(dTable.name));
        }
    }

    public void autoFixDefine(AllDefine defineToFix) {
        Set<String> currentRemains = defineToFix.getTableNames();
        AllType firstTryType = defineToFix.resolveType(ViewFilter.FULL_DEFINE);
        for (DTable dTable : dTables.values()) {
            boolean contains = currentRemains.remove(dTable.name);

            Table tableToFix;
            if (contains) {
                tableToFix = defineToFix.getTable(dTable.name);
            } else {
                tableToFix = defineToFix.newTable(dTable.name);
                Logger.verbose("new table " + tableToFix.fullName());
            }

            try {
                TTable currentTableType = firstTryType.getTTable(dTable.name);
                dTable.autoFixDefine(tableToFix, currentTableType);
            } catch (Throwable e) {
                throw new AssertionError(dTable.name + ", 根据这个表里的数据来猜测表结构和类型出错，看是不是手动在xml里声明一下", e);
            }
        }

        for (String currentRemain : currentRemains) {
            // 被ViewFilter忽略的Table，不应该被修复，所以只有被accept的才可能会被删除
            defineToFix.removeTable(currentRemain);
            Logger.verbose("delete table " + defineToFix.fullName() + "." + currentRemain);
        }
    }

    private static boolean acceptSheet(EFileFormat format, File file, String sheetName) {
//        if (format == EFileFormat.EXCEL) {
        if (!sheetName.isEmpty()) {
            // 只接受首字母是英文字母的页签
            char firstChar = sheetName.charAt(0);
            return ('a' <= firstChar && firstChar <= 'z')
                    || ('A' <= firstChar && firstChar <= 'Z');
        }
        return false;
//        }
//        return true;
    }

}
