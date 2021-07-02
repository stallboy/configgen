package configgen.util;

import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExcelReader {
    private static final DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale

    public static List<List<String>> readSheet(Sheet sheet, FormulaEvaluator evaluator) {
        int lastRoleNum = sheet.getLastRowNum();
        List<List<String>> sheetValue = new ArrayList<>(lastRoleNum + 1);
        for (int rowIndex = 0; rowIndex <= lastRoleNum; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            boolean hasValue = false;
            int lastCellNum = row.getLastCellNum();
            List<String> rowValue = new ArrayList<>(lastCellNum + 1);
            for (int i = 0; i <= lastCellNum; i++) {
                Cell cell = row.getCell(i);
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
