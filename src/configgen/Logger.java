package configgen;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
    private static boolean verboseEnabled = false;

    public static void enableVerbose(boolean enable) {
        verboseEnabled = enable;
    }

    public static void verbose(String s) {
        if (verboseEnabled) {
            log(s);
        }
    }

    private final static SimpleDateFormat df = new SimpleDateFormat("HH.mm.ss.SSS");

    public static void log(String s) {
        System.out.println(df.format(Calendar.getInstance().getTime()) + ": " + s);
    }
}
