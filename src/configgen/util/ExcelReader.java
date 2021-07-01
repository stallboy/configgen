package configgen.util;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExcelReader {
    private static final DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale

    public static List<List<String>> readFromFile(File file) throws IOException, InvalidFormatException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            XSSFSheet sheet = getDataSheet(file, workbook);

            return readSheet(sheet, evaluator);
        }
    }

    // 约定excel中页签和文件名同名的才是数据页，有且只有一个同名的页签
    private static XSSFSheet getDataSheet(File file, XSSFWorkbook workbook) {
        String configName = file.getName();
        int i = configName.lastIndexOf('.');
        if (i > 0) {
            configName = configName.substring(0, i);
        }
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
            String sheetName = sheet.getSheetName().trim();
            if (configName.equals(sheetName)) {
                return sheet;
            }
        }

        throw new AssertionError(file.getAbsolutePath() + ": Excel文件中必须存在一个和文件名同名的页签");
    }

    private static List<List<String>> readSheet(XSSFSheet sheet, FormulaEvaluator evaluator) {
        int lastRoleNum = sheet.getLastRowNum();
        List<List<String>> sheetValue = new ArrayList<>(lastRoleNum + 1);
        for (int rowIndex = 0; rowIndex <= lastRoleNum; rowIndex++) {
            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            boolean hasValue = false;
            int lastCellNum = row.getLastCellNum();
            List<String> rowValue = new ArrayList<>(lastCellNum + 1);
            for (int i = 0; i <= lastCellNum; i++) {
                XSSFCell cell = row.getCell(i);
                String cellVal = formatter.formatCellValue(cell, evaluator);
                if (!hasValue && !cellVal.isEmpty()) {
                    hasValue = true;
                }
                rowValue.add(cellVal);
            }

            if (!hasValue) {
                rowValue = Collections.emptyList();
            }
            sheetValue.add(rowValue);
        }

        return sheetValue;
    }
}
