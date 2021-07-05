package configgen.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelWriter {

    public static void writeToFile(List<SheetData> sheetDataList) throws IOException, InvalidFormatException {
        if (sheetDataList.size() == 1) {
            ExcelWriter.writeToFile(sheetDataList.get(0).file, sheetDataList);
        } else if (!sheetDataList.isEmpty()) {
            Map<File, List<SheetData>> fileMap = new LinkedHashMap<>();
            for (SheetData sheetData : sheetDataList) {
                List<SheetData> lst = fileMap.computeIfAbsent(sheetData.file, k -> new ArrayList<>());
                lst.add(sheetData);
            }

            for (Map.Entry<File, List<SheetData>> e : fileMap.entrySet()) {
                writeToFile(e.getKey(), e.getValue());
            }
        }
    }

    public static void writeToFile(File file, List<SheetData> sheetDataList) throws IOException, InvalidFormatException {
        Workbook workbook = null;
        try {
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    workbook = WorkbookFactory.create(fis);
                }
            } else {
                boolean isXls = file.getName().endsWith(".xls");
                workbook = isXls ? new HSSFWorkbook() : new XSSFWorkbook();
            }

            for (SheetData sheetData : sheetDataList) {
                int sheetIndex = workbook.getSheetIndex(sheetData.sheetName);
                if (sheetIndex >= 0) {
                    workbook.removeSheetAt(sheetIndex);
                }
                Sheet sheet = workbook.createSheet(sheetData.sheetName);
                int rowCnt = 0;
                for (List<String> rowData : sheetData.rows) {
                    Row row = sheet.createRow(rowCnt++);
                    int columnCnt = 0;
                    for (String cellData : rowData) {
                        Cell cell = row.createCell(columnCnt++);
                        if (!cellData.isEmpty()) {
                            cell.setCellValue(cellData);
                        }
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }
}
