package configgen.util;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface SheetHandler {

    List<SheetData> readFromFile(File file, String encoding, ReadFilter filter) throws IOException, InvalidFormatException;

    void writeToFile(List<SheetData> sheetDataList, String encoding) throws IOException, InvalidFormatException;

    class CSVHandler implements SheetHandler {

        @Override
        public List<SheetData> readFromFile(File file, String encoding, ReadFilter filter) throws IOException {
            String fileName = file.getName();
            String sheetName = fileName;
            int i = fileName.lastIndexOf('.');
            if (i >= 0) {
                sheetName = fileName.substring(0, i);
            }
            if (!filter.acceptSheet(EFileFormat.CSV, file, sheetName)) {
                return Collections.emptyList();
            }
            List<List<String>> rows = CSVParser.readFromFile(file.toPath(), encoding);

            return Collections.singletonList(new SheetData(EFileFormat.CSV, file, sheetName, rows));
        }

        @Override
        public void writeToFile(List<SheetData> sheetDataList, String encoding) throws IOException {
            for (SheetData sheetData : sheetDataList) {
                CSVWriter.writeToFile(sheetData.file, encoding, sheetData.rows);
            }
        }
    }


    class ExcelHandler implements SheetHandler {

        @Override
        public List<SheetData> readFromFile(File file, String encoding, ReadFilter filter) throws IOException, InvalidFormatException {
            List<SheetData> sheetDataList = new ArrayList<>();
            try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                    Sheet sheet = workbook.getSheetAt(sheetIndex);
                    String sheetName = sheet.getSheetName().trim();
                    if (!filter.acceptSheet(EFileFormat.EXCEL, file, sheetName)) {
                        continue;
                    }

                    List<List<String>> rows = ExcelReader.readSheet(sheet, evaluator);
                    sheetDataList.add(new SheetData(EFileFormat.EXCEL, file, sheetName, rows));
                }
            }

            return sheetDataList;
        }

        @Override
        public void writeToFile(List<SheetData> sheetDataList, String encoding)
                throws IOException, InvalidFormatException {
            ExcelWriter.writeToFile(sheetDataList);
        }
    }


    interface ReadFilter {

        boolean acceptSheet(EFileFormat format, File file, String sheetName);

    }

}
