package configgen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

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

    private static boolean printNotFoundI18n = false;

    public static void enablePrintNotFound18n() {
        printNotFoundI18n = true;
    }

    public static boolean isPrintNotFoundI18n() {
        return printNotFoundI18n;
    }

    private final static SimpleDateFormat df = new SimpleDateFormat("HH.mm.ss.SSS");
    private static long time;
    private static long firstTime;

    public static void log(String s) {
        System.out.println(s);
    }

    public static void mm(String step) {
        System.gc();
        if (verboseEnabled) {
            long memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
            String elapse;
            if (time == 0) {
                elapse = df.format(Calendar.getInstance().getTime());
                time = System.currentTimeMillis();
                firstTime = time;
            } else {
                long old = time;
                time = System.currentTimeMillis();
                elapse = String.format("%d/%d seconds", TimeUnit.MILLISECONDS.toSeconds(time - old), TimeUnit.MILLISECONDS.toSeconds(time - firstTime));
            }
            System.out.printf("%s\t use %dm\t %s\n", step, memory, elapse);
        }
    }

}
