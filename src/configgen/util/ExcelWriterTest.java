package configgen.util;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ExcelWriterTest {

    @Test
    public void testWriteExcel() {
        String fileName = "test.xlsx";

        List<List<String>> rows = Arrays.asList(
                Arrays.asList("标题1", "标题2", "策划说明", "标题3"),
                Arrays.asList("title1", "title2", "", "title3"),
                Arrays.asList(String.valueOf(1), "test", "abc\r\ndef", ""),
                Arrays.asList(String.valueOf(2), "test2", "hello\nworld", "")
        );

        SheetUtils.writeToFile(SheetData.valueOf(new File(fileName), rows), "UTF-8");
    }

}
