package configgen.data;

import configgen.util.CSV;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class Test {

    public static void main(String[] args) throws IOException {
        test("aa, bb", 1, 0, "aa", " bb");
        test("aa, bb\r\n", 1, 0, "aa", " bb");
        test("aa, bb\r", 1, 0, "aa", " bb\r");
        test("aa, bb,", 1, 0, "aa", " bb", "");
        test("\"aa\", bb", 1, 0, "aa", " bb");
        test("\"aa\"123, bb", 1, 0, "aa123", " bb");
        test("aa, bb\rcc,dd", 1, 0, "aa", " bb\rcc", "dd");
        test("aa,\"bb\r\ncc\",dd", 1, 0, "aa", "bb\r\ncc", "dd");

        test("aa, bb\r\ncc,dd", 2, 1, "cc", "dd");
        test("aa, bb\r\ncc,\"dd", 2, 1, "cc", "dd");

        septest("aa123", "aa", 123);
        septest("a123b1", "a123b", 1);
        septest("123baaaaaa1", "123baaaaaa", 1);
        septest("aa12@123", "aa12");
        septest("123abc@", "123abc");
        septestf("a123a");
        septestf("1231");

        // need strip bom
        System.out.println(CSV.parse(new InputStreamReader(new FileInputStream("text.csv"), StandardCharsets.UTF_8)));
    }

    private static void test(String source, int rowcnt, int testrow, String... row) throws IOException {
        List<List<String>> r = CSV.parse(new StringReader(source));
        equal(rowcnt, r.size());
        List<String> a = r.get(testrow);
        equal(row.length, a.size());
        int i = 0;
        for (String c : row) {
            equal(c, a.get(i++));
        }
    }

    private static void equal(Object expected, Object actual) {
        if (!expected.equals(actual))
            throw new RuntimeException("expected " + expected + ", actual " + actual);
    }

    private static void septest(String a, String f, int n) {
        GuessHelper.Sep s = GuessHelper.trySep(a);
        equal(GuessHelper.SepType.IntPostfix, s.type);
        equal(f, s.columnName);
        equal(n, s.num);
    }

    private static void septest(String a, String f) {
        GuessHelper.Sep s = GuessHelper.trySep(a);
        equal(GuessHelper.SepType.BeanPrefix, s.type);
        equal(f, s.columnName);
    }

    private static void septestf(String a) {
        GuessHelper.Sep s = GuessHelper.trySep(a);
        equal(GuessHelper.SepType.None, s.type);
    }
}