package configgen.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CSVParserTest {

    @Test
    void parse() {
        testOneLine("aa, bb", "aa", " bb");
        testOneLine("aa, bb,", "aa", " bb", "");
        test("aa, bb\r\ncc,\"dd", 2, 1, "cc", "dd");
    }

    @Test
    void useCRLF_MakeNewLine() {
        testOneLine("aa, bb\r\n", "aa", " bb");
        test("aa, bb\r\ncc,dd", 2, 1, "cc", "dd");
    }

    @Test
    void useCR_Or_LF_Alone_Not_MakeNewLine() {
        testOneLine("aa, bb\r", "aa", " bb\r");
        testOneLine("aa, bb\n", "aa", " bb\n");
        testOneLine("aa, bb\rcc,dd", "aa", " bb\rcc", "dd");
        test("aa, bb\r\ncc,dd", 2, 1, "cc", "dd");
    }

    @Test
    void useDoubleQuoteToEscape() {
        testOneLine("\"aa\", bb", "aa", " bb");
        testOneLine("\"a,a\", bb", "a,a", " bb");
        testOneLine("aa,\"bb\r\ncc\",dd", "aa", "bb\r\ncc", "dd");
    }

    @Test
    void oneDoubleQuoteInMiddle_Ignore() {
        testOneLine("\"aa\"123, bb", "aa123", " bb");
    }

    private void testOneLine(String source, String... row) {
        test(source, 1, 0, row);
    }

    private void test(String source, int rowcnt, int testrow, String... row) {
        List<List<String>> r = CSVParser.parse(source);
        assertEquals(rowcnt, r.size());
        List<String> a = r.get(testrow);
        assertEquals(row.length, a.size());
        int i = 0;
        for (String c : row) {
            assertEquals(c, a.get(i++));
        }
    }

}