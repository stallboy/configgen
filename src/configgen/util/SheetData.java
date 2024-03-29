package configgen.util;

import java.io.File;
import java.util.List;

public class SheetData {
    public final EFileFormat format;
    public final File file;
    public final String codeName;
    public final String sheetName;
    public final List<List<String>> rows;

    public SheetData(EFileFormat format, File file, String sheetName, List<List<String>> rows, String codeName) {
        this.format = format;
        this.file = file;
        this.sheetName = sheetName;
        this.rows = rows;
        this.codeName = codeName;
    }

    // 用于测试
    public static SheetData valueOf(File file, List<List<String>> rows) {
        EFileFormat format = SheetUtils.getFileFormat(file);
        if (format == null) {
            throw new IllegalStateException("Unsupported format. file = " + file);
        }

        String sheetName = file.getName();
        int dotIndex = sheetName.lastIndexOf(".");
        if (dotIndex >= 0) {
            sheetName = sheetName.substring(0, dotIndex);
        }
        String codeName = sheetName;
        return new SheetData(format, file, sheetName, rows, codeName);
    }

}
