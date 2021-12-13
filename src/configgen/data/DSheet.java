package configgen.data;

import configgen.Node;
import configgen.define.AllDefine;
import configgen.define.Table;
import configgen.util.CSVParser;
import configgen.util.EFileFormat;
import configgen.util.SheetData;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 持一个DTable拆分到多个的表格中配置，每个表格用DSheet表示
 * 约定表格必须在同一个文件夹下，且名称为 xxx 或者 xxx_0,xxx_1,xxx_2,xxx_3 ...
 * 比如task表，
 * 如果是csv配置，可以拆分成：task.csv(同task_0.csv)，task_1.csv，task_2.csv
 * 如果是excel配置，excel中的页签名称可以拆分成：task(同task_0)，task_1，task_2
 */
public class DSheet extends Node {
    private final String configName;
    private final int tableIndex;

    private final List<String> descLine;
    private final List<String> nameLine;
    /**
     * csv HeadRow行之后的所有的数据， csv -> list.list.str
     */
    private final List<List<String>> recordList;
    private final boolean isColumnMode;


    private static int HeadRow;

    static {
        String row = System.getProperty("configgen.headrow");
        if (row == null || row.equals("2")) {
            HeadRow = 2;
        } else if (row.equals("3")) {
            HeadRow = 3; //第三行可以是类型信息，随便，这里不会读取这个数据，会忽略掉，也就是说类型的权威数据在xml里
        } else {
            System.err.printf("-Dconfiggen.headrow，设置为[%s], 它只能设置为2或3，不设置的话默认是2\n", row);
        }
    }

    public static int getHeadRow() {
        return HeadRow;
    }

    static DSheet create(AllDefine allDefine, Node parent, SheetData sheetData) {
        String codeName = sheetData.codeName;
        EFileFormat format = sheetData.format;
        File file = sheetData.file;

        String tableName;
        int tableIndex;
        int i = codeName.lastIndexOf("_");
        if (i < 0) {
            tableName = codeName.trim();
            tableIndex = 0;
        } else {
            String postfix = codeName.substring(i + 1).trim();
            try {
                tableIndex = Integer.parseInt(postfix);
                tableName = codeName.substring(0, i).trim();
            } catch (NumberFormatException ignore) {
                tableName = codeName.trim();
                tableIndex = 0;
            }
        }

        if (tableName.isEmpty()) {
            if (format == EFileFormat.CSV) {
                throw new AssertionError("根据表名解析出的tableName为空, file = " + file);
            }
            throw new AssertionError("根据sheet名称解析出的tableName为空， file = " + file + ", sheetName = " + codeName);
        }

        if (tableIndex < 0) {
            if (format == EFileFormat.CSV) {
                throw new AssertionError("根据表名解析出的tableIndex为负数, file = " + file);
            }
            throw new AssertionError("根据sheet名称解析出的tableIndex为负数， file = " + file + ", sheetName = " + codeName);
        }

        String packageName = allDefine.childDataPathToPkgName(file.getParentFile().toPath());
        //将表名转成小写，保持原来的大小写更合适吧？
        String configName = packageName + "." + tableName.toLowerCase();
        String sheetId = getSheetId(allDefine.getDataDir(), sheetData);
        Table tableDefine = allDefine.getTable(configName);
        boolean isColumnMode = tableDefine != null && tableDefine.isColumnMode;
        return new DSheet(parent, sheetId, sheetData, configName, tableIndex, isColumnMode);
    }

    private DSheet(Node parent, String sheetId, SheetData sheetData, String configName, int tableIndex, boolean isColumnMode) {
        super(parent, sheetId);

        EFileFormat format = sheetData.format;
        this.configName = configName;
        this.tableIndex = tableIndex;
        this.isColumnMode = isColumnMode;

        List<List<String>> rows = sheetData.rows;
        if (isColumnMode) {
            if (rows.size() < 1) {
                throw new AssertionError(fullName() + " 数据行数小于1");
            }

            rows = convertColumnMode(rows);
        }


        if (rows.size() < 2) {
            throw new AssertionError(fullName() + " 数据行数小于2");
        }

        descLine = rows.get(0);
        nameLine = rows.get(1);
        recordList = adjustRecords(format, nameLine, rows);
    }

    // 转列模式为行，之后统一处理
    private static List<List<String>> convertColumnMode(List<List<String>> original) {
        int origCol_resRow_Cnt = original.get(0).size(); //一般很小
        int origRow_resCol_Cnt = original.size(); //一般很大

        List<List<String>> res = new ArrayList<>(origCol_resRow_Cnt);
        for (int origCol_resRow = 0; origCol_resRow < origCol_resRow_Cnt; origCol_resRow++) {
            List<String> row = new ArrayList<>(origRow_resCol_Cnt);

            for (List<String> originalRow : original) {
                String cell = "";
                if (origCol_resRow < originalRow.size()){
                    cell = originalRow.get(origCol_resRow);
                }
                row.add(cell);
            }

            if (CSVParser.checkRecordEmpty(row)) {
                row = Collections.emptyList();
            }
            res.add(row);
        }

        return res;
    }

    // 读取excel数据时使用, 防止后续的读取不到数据出现数组越界
    private static List<List<String>> adjustRecords(EFileFormat format, List<String> nameLine, List<List<String>> rows) {
        if (format != EFileFormat.EXCEL) {
            return rows.subList(HeadRow, rows.size());
        }

        int usefulColumnsCnt = getUsefulColumnCnt(nameLine);
        // 根据有效列数量，填充未满的记录列，防止后续读取数据时数组越界，excel才会有这种问题
        for (List<String> row : rows) {
            if (row.isEmpty()) {
                continue;
            }
            while (row.size() < usefulColumnsCnt) {
                row.add("");
            }
        }

        return rows.subList(HeadRow, rows.size());
    }

    // 根据nameLine计算出有效列数量
    private static int getUsefulColumnCnt(List<String> nameLine) {
        int nameColumnsCnt = nameLine.size();
        int uselessColumnsCnt = 0;
        for (int i = nameLine.size() - 1; i >= 0; i--) {
            if (nameLine.get(i).isEmpty()) {
                uselessColumnsCnt++;
            } else {
                break;
            }
        }
        return nameColumnsCnt - uselessColumnsCnt;
    }

    public void assertCompatible(DSheet target) {
        int usefulColumnCnt = getUsefulColumnCnt(nameLine);
        int targetUsefulColumnCnt = getUsefulColumnCnt(target.nameLine);
        if (targetUsefulColumnCnt != usefulColumnCnt) {
            throw new AssertionError("表格的列数不匹配. firstSheet = " + fullName() + "[共" + usefulColumnCnt + " 列]"
                                             + ", targetSheet = " + target.fullName() + "[共" + targetUsefulColumnCnt + " 列]");
        }
        for (int i = 0; i < usefulColumnCnt; i++) {
            String columnName = nameLine.get(i);
            String targetColumnName = target.nameLine.get(i);
            if (!targetColumnName.equalsIgnoreCase(columnName)) {
                throw new AssertionError("表格的列名称不匹配. " +
                                                 "firstSheet = " + fullName() + "[第 " + i + " 列][" + columnName + "]" +
                                                 ", targetSheet = " + target.fullName() + "[第 " + targetUsefulColumnCnt + " 列][" + targetColumnName + "]");
            }
        }
    }

    private static String getSheetId(Path topDir, SheetData data) {
        if (data.format == EFileFormat.EXCEL) {
            return topDir.relativize(data.file.toPath()) + "/" + data.sheetName;
        } else {
            return topDir.relativize(data.file.toPath()).toString();
        }
    }

    public String getConfigName() {
        return configName;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public List<String> getDescLine() {
        return descLine;
    }

    public List<String> getNameLine() {
        return nameLine;
    }

    public List<List<String>> getRecordList() {
        return recordList;
    }

    public boolean isColumnMode() {
        return isColumnMode;
    }

}
