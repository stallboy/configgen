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

        return EFileFormat.NONE;
    }

    public static List<SheetData> readFromFile(File file, SheetHandler.ReadOption option) {
        try {
            return readFromFile0(file, option);
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static List<SheetData> readFromFile0(File file, SheetHandler.ReadOption option)
            throws IOException, InvalidFormatException {
        EFileFormat format = getFileFormat(file);
        if (format == EFileFormat.NONE) {
            return Collections.emptyList();
        }

        SheetHandler sheetHandler = sheetHandlerMap.get(format);
        if (sheetHandler == null) {
            throw new IllegalStateException("Unsupported EFileFormat. format = " + format + ", file = " + file);
        }

        return sheetHandler.readFromFile(file, option);
    }

    public static void writeToFile(File file, List<List<String>> rows, SheetHandler.WriteOption option) {
        writeToFile(SheetData.valueOf(file, rows), option);
    }

    public static void writeToFile(File file, String sheetName, List<List<String>> rows, SheetHandler.WriteOption option) {
        writeToFile(SheetData.valueOf(file, sheetName, rows), option);
    }

    public static void writeToFile(SheetData sheetData, SheetHandler.WriteOption option) {
        writeToFile(Collections.singletonList(sheetData), option);
    }

    public static void writeToFile(List<SheetData> sheetDataList, SheetHandler.WriteOption option) {
        try {
            writeToFile0(sheetDataList, option);
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void writeToFile0(List<SheetData> sheetDataList, SheetHandler.WriteOption option)
            throws IOException, InvalidFormatException {
        if (sheetDataList.size() == 1) {
            writeToFile0(sheetDataList.get(0).format, sheetDataList, option);
        }
        Map<EFileFormat, List<SheetData>> formatMap = new EnumMap<>(EFileFormat.class);
        for (SheetData sheetData : sheetDataList) {
            List<SheetData> lst = formatMap.get(sheetData.format);
            if (lst == null) {
                formatMap.put(sheetData.format, lst = new ArrayList<>());
            }
            lst.add(sheetData);
        }

        for (Map.Entry<EFileFormat, List<SheetData>> e : formatMap.entrySet()) {
            writeToFile0(e.getKey(), e.getValue(), option);
        }
    }

    private static void writeToFile0(EFileFormat format, List<SheetData> sheetDataList, SheetHandler.WriteOption option)
            throws IOException, InvalidFormatException {
        SheetHandler sheetHandler = sheetHandlerMap.get(format);
        if (sheetHandler == null) {
            throw new IllegalStateException("EFileFormat not support. format = " + format +
                    ", file = " + sheetDataList.get(0).file);
        }
        sheetHandler.writeToFile(sheetDataList, option);
    }

}
