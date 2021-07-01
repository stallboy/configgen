package configgen.data;

import configgen.util.CSVParser;
import configgen.util.CSVWriter;
import configgen.util.ExcelReader;
import configgen.util.ExcelWriter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DataFormatUtils {

    public static EFileFormat getFileFormat(File file) {
        String fileName = file.getName();
        String ext = "";
        int i = fileName.lastIndexOf('.');
        if (i >= 0) {
            ext = fileName.substring(i + 1);
        }

        if ("csv".equalsIgnoreCase(ext)) {
            return EFileFormat.CSV;
        } else if ("xlsx".equalsIgnoreCase(ext)) {
            return EFileFormat.EXCEL;
        }

        return EFileFormat.NONE;
    }

    public static List<List<String>> readFromFile(File file, String encoding) {
        EFileFormat fileFormat = getFileFormat(file);

        return readFromFile(fileFormat, file, encoding);
    }

    public static List<List<String>> readFromFile(EFileFormat fileFormat, File file, String encoding) {
        try {
            return readFromFile0(fileFormat, file, encoding);
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static List<List<String>> readFromFile0(EFileFormat fileFormat, File file, String encoding)
            throws IOException, InvalidFormatException {
        switch (fileFormat) {
            case CSV:
                return CSVParser.readFromFile(file.toPath(), encoding);
            case EXCEL:
                return ExcelReader.readFromFile(file);
            default:
                throw new IllegalStateException("EFileFormat not support. format = " + fileFormat + ", file = " + file.getAbsolutePath());
        }
    }


    public static void writeToFile(File file, String encoding, List<List<String>> rows) {
        EFileFormat fileFormat = getFileFormat(file);

        writeToFile(fileFormat, file, encoding, rows);
    }

    public static void writeToFile(EFileFormat fileFormat, File file, String encoding, List<List<String>> rows) {
        try {
            writeToFile0(fileFormat, file, encoding, rows);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void writeToFile0(EFileFormat fileFormat, File file, String encoding, List<List<String>> rows) throws IOException {
        switch (fileFormat) {
            case CSV:
                CSVWriter.writeToFile(file, encoding, rows);
                break;
            case EXCEL:
                ExcelWriter.writeToFile(file, rows);
                break;
            default:
                throw new IllegalStateException("EFileFormat not support. format = " + fileFormat + ", file = " + file.getAbsolutePath());
        }
    }

}
