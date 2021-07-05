package configgen.util;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SheetUtils {

    private static final EnumMap<EFileFormat, SheetHandler> sheetHandlerMap = new EnumMap<>(EFileFormat.class);

    static {
        sheetHandlerMap.put(EFileFormat.CSV, new SheetHandler.CSVHandler());
        sheetHandlerMap.put(EFileFormat.EXCEL, new SheetHandler.ExcelHandler());
    }

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
        } else if ("xls".equalsIgnoreCase(ext)) {
            return EFileFormat.EXCEL;
        }

        return null;
    }

    public static List<SheetData> readFromFile(File file, String encoding, SheetHandler.ReadFilter filter) {
        try {
            return readFromFile0(file, encoding, filter);
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static List<SheetData> readFromFile0(File file, String encoding, SheetHandler.ReadFilter filter)
            throws IOException, InvalidFormatException {
        EFileFormat format = getFileFormat(file);
        if (format == null) {
            return Collections.emptyList();
        }

        SheetHandler sheetHandler = sheetHandlerMap.get(format);
        if (sheetHandler == null) {
            throw new IllegalStateException("Unsupported EFileFormat. format = " + format + ", file = " + file);
        }

        return sheetHandler.readFromFile(file, encoding, filter);
    }

    public static void writeToFile(File file, List<List<String>> rows, String encoding) {
        writeToFile(SheetData.valueOf(file, rows), encoding);
    }

    public static void writeToFile(File file, String sheetName, List<List<String>> rows, String encoding) {
        writeToFile(SheetData.valueOf(file, sheetName, rows), encoding);
    }

    public static void writeToFile(SheetData sheetData, String encoding) {
        writeToFile(Collections.singletonList(sheetData), encoding);
    }

    public static void writeToFile(List<SheetData> sheetDataList, String encoding) {
        try {
            writeToFile0(sheetDataList, encoding);
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void writeToFile0(List<SheetData> sheetDataList, String encoding)
            throws IOException, InvalidFormatException {
        if (sheetDataList.size() == 1) {
            writeToFile0(sheetDataList.get(0).format, sheetDataList, encoding);
        }
        Map<EFileFormat, List<SheetData>> formatMap = new EnumMap<>(EFileFormat.class);
        for (SheetData sheetData : sheetDataList) {
            List<SheetData> lst = formatMap.computeIfAbsent(sheetData.format, k -> new ArrayList<>());
            lst.add(sheetData);
        }

        for (Map.Entry<EFileFormat, List<SheetData>> e : formatMap.entrySet()) {
            writeToFile0(e.getKey(), e.getValue(), encoding);
        }
    }

    private static void writeToFile0(EFileFormat format, List<SheetData> sheetDataList, String encoding)
            throws IOException, InvalidFormatException {
        SheetHandler sheetHandler = sheetHandlerMap.get(format);
        if (sheetHandler == null) {
            throw new IllegalStateException("EFileFormat not support. format = " + format +
                    ", file = " + sheetDataList.get(0).file);
        }
        sheetHandler.writeToFile(sheetDataList, encoding);
    }

}
