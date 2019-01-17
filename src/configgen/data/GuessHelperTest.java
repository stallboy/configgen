package configgen.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuessHelperTest {

    @Test
    void trySep() {
        septest("aa123", "aa", 123);
        septest("a123b1", "a123b", 1);
        septest("123baaaaaa1", "123baaaaaa", 1);
        septest("aa12@123", "aa12");
        septest("123abc@", "123abc");
        septestf("a123a");
        septestf("1231");
    }


    private void septest(String a, String f, int n) {
        GuessHelper.Sep s = GuessHelper.trySep(a);
        assertEquals(GuessHelper.SepType.IntPostfix, s.type);
        assertEquals(f, s.columnName);
        assertEquals(n, s.num);
    }

    private void septest(String a, String f) {
        GuessHelper.Sep s = GuessHelper.trySep(a);
        assertEquals(GuessHelper.SepType.BeanPrefix, s.type);
        assertEquals(f, s.columnName);
    }

    private void septestf(String a) {
        GuessHelper.Sep s = GuessHelper.trySep(a);
        assertEquals(GuessHelper.SepType.None, s.type);
    }
}