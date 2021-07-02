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

    List<SheetData> readFromFile(File file, ReadOption option) throws IOException, InvalidFormatException;

    void writeToFile(List<SheetData> sheetDataList, WriteOption option) throws IOException, InvalidFormatException;

    class CSVHandler implements SheetHandler {

        @Override
        public List<SheetData> readFromFile(File file, ReadOption option) throws IOException, InvalidFormatException {
            String fileName = file.getName();
            String sheetName = fileName;
            int i = fileName.lastIndexOf('.');
            if (i >= 0) {
                sheetName = fileName.substring(0, i);
            }
            if (!option.acceptSheet(EFileFormat.CSV, file, sheetName)) {
                return Collections.emptyList();
            }
            List<List<String>> rows = CSVParser.readFromFile(file.toPath(), option.dataEncoding());

            return Collections.singletonList(new SheetData(EFileFormat.CSV, file, sheetName, rows));
        }

        @Override
        public void writeToFile(List<SheetData> sheetDataList, WriteOption option) throws IOException, InvalidFormatException {
            for (SheetData sheetData : sheetDataList) {
                CSVWriter.writeToFile(sheetData.file, option.dataEncoding(), sheetData.rows);
            }
        }
    }


    class ExcelHandler implements SheetHandler {

        @Override
        public List<SheetData> readFromFile(File file, ReadOption option) throws IOException, InvalidFormatException {
            List<SheetData> sheetDataList = new ArrayList<>();
            try (Workbook workbook = WorkbookFactory.create(file)) {
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                    Sheet sheet = workbook.getSheetAt(sheetIndex);
                    String sheetName = sheet.getSheetName().trim();
                    //暂时约定已下划线开头的sheet为非数据sheet，不读
                    if (!option.acceptSheet(EFileFormat.EXCEL, file, sheetName)) {
                        continue;
                    }

                    List<List<String>> rows = ExcelReader.readSheet(sheet, evaluator);
                    sheetDataList.add(new SheetData(EFileFormat.EXCEL, file, sheetName, rows));
                }
            }

            return sheetDataList;
        }

        @Override
        public void writeToFile(List<SheetData> sheetDataList, WriteOption option)
                throws IOException, InvalidFormatException {
            ExcelWriter.writeToFile(sheetDataList);
        }
    }


    interface ReadOption {

        String dataEncoding();

        boolean acceptSheet(EFileFormat format, File file, String sheetName);

    }

    class DefaultReadOption implements ReadOption {
        private final String dataEncoding;

        public DefaultReadOption(String dataEncoding) {
            this.dataEncoding = dataEncoding;
        }

        @Override
        public String dataEncoding() {
            return this.dataEncoding;
        }

        @Override
        public boolean acceptSheet(EFileFormat format, File file, String sheetName) {
            return true;
        }
    }

    interface WriteOption {

        String dataEncoding();

    }

    class DefaultWriteOption implements WriteOption {
        private final String dataEncoding;

        public DefaultWriteOption(String dataEncoding) {
            this.dataEncoding = dataEncoding;
        }

        @Override
        public String dataEncoding() {
            return this.dataEncoding;
        }
    }

}
