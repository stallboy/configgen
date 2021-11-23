package configgen.util;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SheetUtils {

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

    public static List<SheetData> readFromFile(File file, String encoding) {
        try {
            EFileFormat format = getFileFormat(file);
            if (format == null) {
                return Collections.emptyList();
            }

            return format.readFromFile(file, encoding);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void writeToFile(SheetData sheetData, String encoding) {
        writeToFile(Collections.singletonList(sheetData), encoding);
    }

    public static void writeToFile(List<SheetData> sheetDataList, String encoding) {
        try {
            if (sheetDataList.size() == 1) {
                sheetDataList.get(0).format.writeToFile(sheetDataList, encoding);
            }
            Map<EFileFormat, List<SheetData>> formatMap = new EnumMap<>(EFileFormat.class);
            for (SheetData sheetData : sheetDataList) {
                List<SheetData> lst = formatMap.computeIfAbsent(sheetData.format, k -> new ArrayList<>());
                lst.add(sheetData);
            }

            for (Map.Entry<EFileFormat, List<SheetData>> e : formatMap.entrySet()) {
                e.getKey().writeToFile(e.getValue(), encoding);
            }

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
