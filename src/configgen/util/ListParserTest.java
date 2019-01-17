package configgen.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListParserTest {

    @Test
    void parseList() {
        test("aa,bb", "aa", "bb");
    }

    @Test
    void prefixSeparator_Counts() {
        test(",aa,bb", "", "aa", "bb");
    }

    @Test
    void surfixSeparator_NotCounts() {
        test("aa,bb,", "aa", "bb");
    }

    @Test
    void useQuoteToEscapeSeperatorInString() {
        test("\"a,a\", bb", "a,a", " bb");
        test("\"aa\", bb", "aa", " bb");
    }

    @Test
    void whitespace_Counts() {
        test("aa, bb", "aa", " bb");
        test(" aa, bb ", " aa", " bb ");
    }


    private void test(String source, String... row) {
        List<String> a = ListParser.parseList(source, ',');
        assertEquals(row.length, a.size());
        int i = 0;
        for (String c : row) {
            assertEquals(c, a.get(i++));
        }
    }

}