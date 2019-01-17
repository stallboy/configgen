package configgen.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NestListParserTest {

    @Test
    void parseNestList() {
        t1("a,b", "a", "b");
        t1("QingGong(10,(13300100,13300200)), RanSe(30,3)", "QingGong(10,(13300100,13300200))", "RanSe(30,3)");
        t1("QingGong(10, (13300100, 13300200)) , RanSe(30,3) ", "QingGong(10, (13300100, 13300200))", "RanSe(30,3)");
    }

    @Test
    void prefixWhitespace_Ignore() {
        t1("a, b, c", "a", "b", "c");
    }

    @Test
    void surfixWhitespace_NotIgnore() {
        t1("a, b ", "a", "b ");
    }

    @Test
    void parentheses_AsOne() {
        t1("(a,b)", "a,b");
    }

    @Test
    void firstLayerParentheses_TakeOff() {
        t1("a,(b,c)", "a", "b,c");
    }

    @Test
    void firstLayerParenthesesWithFunction_Keep() {
        t1("a,f(b,c)", "a", "f(b,c)");
    }

    @Test
    void secondLayerParentheses_Keep() {
        t1("a,(b,(c,d))", "a", "b,(c,d)");
    }

    private void t1(String source, String... row) {
        List<String> a = NestListParser.parseNestList(source);
        assertEquals(row.length, a.size());
        int i = 0;
        for (String c : row) {
            assertEquals(c, a.get(i++));
        }
    }

    @Test
    void parseFunction() {
        t2("a(b,c)", "a", "b,c");
        t2("abc(b,c,d(e,f))", "abc", "b,c,d(e,f)");
        assertThrows(Throwable.class, () -> NestListParser.parseFunction("a,b,c"));
    }

    private void t2(String source, String name, String parameters) {
        List<String> a = NestListParser.parseFunction(source);
        assertEquals(2, a.size());
        assertEquals(name, a.get(0));
        assertEquals(parameters, a.get(1));
    }
}