package configgen.util;

import configgen.data.DataFormatUtils;
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

        DataFormatUtils.writeToFile(new File(fileName), "UTF-8", rows);
    }

}