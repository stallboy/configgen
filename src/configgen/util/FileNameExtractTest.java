package configgen.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class FileNameExtractTest {

    @Test
    public void testExtract_IgnoreFirstChineseCharAndAfter() {

        String e = FileNameExtract.extractFileName("test中文.xlsx");
        assertEquals(e, "test");
    }

    @Test
    public void testExtract_AlsoIgnoreFirstUnderlineBeforeChineseChar() {
        String e = FileNameExtract.extractFileName("test_中文.xlsx");
        assertEquals(e, "test");

        e = FileNameExtract.extractFileName("test__中文.xlsx");
        assertEquals(e, "test_");
    }


    @Test
    public void testExtract_MustPrefixA_Z_or_a_z() {
        String e = FileNameExtract.extractFileName("_test_中文.xlsx");
        assertNull(e);

        e = FileNameExtract.extractFileName("中_test_中文.xlsx");
        assertNull(e);

        e = FileNameExtract.extractFileName("Test_中文.xlsx");
        assertEquals(e, "test");
    }

    @Test
    public void testExtractCan_1Or2_ThenChineseChar() {
        String e = FileNameExtract.extractFileName("test_1_中文.xlsx");
        assertEquals(e, "test_1");

        e = FileNameExtract.extractFileName("test_2中文.xlsx");
        assertEquals(e, "test_2");
    }

    @Test
    public void testExtractPathName(){
        String e = FileNameExtract.extractPathName("test_1_中文/test2_");
        assertEquals(e, "test_1.test2_");

        e = FileNameExtract.extractPathName("test_1_中文/test2_哈哈/a3");
        assertEquals(e, "test_1.test2.a3");
    }

    @Test
    public void testIsFileNameExtractMatch(){
        boolean e = FileNameExtract.isFileNameExtractMatch("test_1_中文", "test_1");
        assertTrue(e);

    }

}
