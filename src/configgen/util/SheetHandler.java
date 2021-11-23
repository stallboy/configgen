package configgen.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface SheetHandler {

    List<SheetData> readFromFile(File file, String encoding) throws IOException;

    void writeToFile(List<SheetData> sheetDataList, String encoding) throws IOException;


}
