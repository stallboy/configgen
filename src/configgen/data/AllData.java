package configgen.data;

import configgen.Logger;
import configgen.Node;
import configgen.define.AllDefine;
import configgen.define.Bean;
import configgen.define.Table;
import configgen.type.AllType;
import configgen.type.TTable;
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
import java.util.concurrent.*;

public class AllData extends Node {
    private final Map<String, DTable> dTables = new HashMap<>();

    public AllData(AllDefine allDefine) {
        super(null, "AllData");

        Path dataDir = allDefine.getDataDir();
        if (!Files.isDirectory(dataDir)) {
            throw new IllegalArgumentException("配置顶级目录必须是目录. dataDir = " + dataDir);
        }

        List<Callable<List<SheetData>>> tasks = new ArrayList<>();
        String encoding = allDefine.getEncoding();
        try {
            //noinspection Convert2Diamond
            Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes a) {

                    if (path.toFile().isHidden()){
                        return FileVisitResult.CONTINUE;
                    }

                    if (path.getFileName().toString().startsWith("~")){
                        return FileVisitResult.CONTINUE;
                    }

                    tasks.add(() -> SheetUtils.readFromFile(path.toFile(), encoding));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        Map<String, List<DSheet>> dSheetMap = new TreeMap<>();

        try(ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())){
            List<Future<List<SheetData>>> futures = executor.invokeAll(tasks);
            for (Future<List<SheetData>> future : futures) {
                List<SheetData>  sheetDataList = future.get();
                for (SheetData sheetData : sheetDataList) {
                    DSheet sheet = DSheet.create(allDefine, AllData.this, sheetData);
                    List<DSheet> sheetList = dSheetMap.computeIfAbsent(sheet.getConfigName(), k -> new ArrayList<>());
                    sheetList.add(sheet);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
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


        for (Table table : defineToFix.getAllTables()) {
            table.autoFixDefine(defineToFix);
        }

        for (Bean bean : defineToFix.getAllBeans()) {
            bean.autoFixDefine(defineToFix);
        }
    }


}
