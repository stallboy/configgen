package configgen.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelWriter {

    public static void writeToFile(File file, List<List<String>> rows) throws IOException {
        String sheetName = file.getName();
        int dotIndex = sheetName.lastIndexOf(".");
        if (dotIndex >= 0) {
            sheetName = sheetName.substring(0, dotIndex);
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        int rowCnt = 0;
        for (List<String> rowData : rows) {
            Row row = sheet.createRow(rowCnt++);
            int columnCnt = 0;
            for (String cellData : rowData) {
                Cell cell = row.createCell(columnCnt++);
                if (!cellData.isEmpty()) {
                    cell.setCellValue(cellData);
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
    }
}
