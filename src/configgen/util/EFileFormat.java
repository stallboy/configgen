package configgen.util;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum EFileFormat implements SheetHandler {

    CSV {
        @Override
        public List<SheetData> readFromFile(File file, String encoding) throws IOException {
            String fileName = file.getName();
            String sheetName = fileName;
            int i = fileName.lastIndexOf('.');
            if (i >= 0) {
                sheetName = fileName.substring(0, i);
            }

            String codeName = FileNameExtract.extractFileName(sheetName);
            if (codeName == null) {
                return Collections.emptyList();
            }
            List<List<String>> rows = CSVParser.readFromFile(file.toPath(), encoding);

            return Collections.singletonList(new SheetData(EFileFormat.CSV, file, sheetName, rows, codeName));
        }

        @Override
        public void writeToFile(List<SheetData> sheetDataList, String encoding) throws IOException {
            for (SheetData sheetData : sheetDataList) {
                CSVWriter.writeToFile(sheetData.file, encoding, sheetData.rows);
            }
        }
    },

    EXCEL {
        @Override
        public List<SheetData> readFromFile(File file, String encoding) throws IOException {
            List<SheetData> sheetDataList = new ArrayList<>();
            try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                    Sheet sheet = workbook.getSheetAt(sheetIndex);
                    String sheetName = sheet.getSheetName().trim();

                    String codeName = FileNameExtract.extractFileName(sheetName);
                    if (codeName == null) {
                        continue;
                    }

                    List<List<String>> rows = ExcelReader.readSheet(sheet, evaluator);
                    sheetDataList.add(new SheetData(EFileFormat.EXCEL, file, sheetName, rows, codeName));
                }
            }
            return sheetDataList;
        }

        @Override
        public void writeToFile(List<SheetData> sheetDataList, String encoding) throws IOException {
            ExcelWriter.writeToFile(sheetDataList);

        }
    }


}
