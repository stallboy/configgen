package configgen;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
    private static boolean verboseEnabled = false;

    public static void enableVerbose() {
        verboseEnabled = true;
    }

    public static void verbose(String s) {
        if (verboseEnabled) {
            log(s);
        }
    }

    public static void printf(String fmt, Object... args) {
        if (verboseEnabled) {
            System.out.printf(fmt, args);
        }
    }

    private static boolean printNotFoundI18n = false;

    public static void enablePrintNotFound18n() {
        printNotFoundI18n = true;
    }

    public static boolean isPrintNotFoundI18n() {
        return printNotFoundI18n;
    }

    private final static SimpleDateFormat df = new SimpleDateFormat("HH.mm.ss.SSS");

    public static void log(String s, Object... args) {
        System.out.println(df.format(Calendar.getInstance().getTime()) + ": " + s);
    }
}
