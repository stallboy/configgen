package configgen;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
    private static boolean verboseEnabled = false;
    private static boolean mmGcEnabled = false;

    public static void enableMmGc() {
        mmGcEnabled = true;
    }

    public static void enableVerbose() {
        verboseEnabled = true;
    }

    public static void verbose(String s) {
        if (verboseEnabled) {
            log(s);
        }
    }

    private final static SimpleDateFormat df = new SimpleDateFormat("HH.mm.ss.SSS");
    private static long time;
    private static long firstTime;

    public static void log(String s) {
        System.out.println(s);
    }

    public static void mm(String step) {
        if (verboseEnabled) {
            if (mmGcEnabled){
                System.gc();
            }
            long memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
            String elapse;
            if (time == 0) {
                elapse = df.format(Calendar.getInstance().getTime());
                time = System.currentTimeMillis();
                firstTime = time;
            } else {
                long old = time;
                time = System.currentTimeMillis();
                elapse = String.format("%.1f/%.1f seconds", (time - old) / 1000f, (time - firstTime) / 1000f);
            }
            System.out.printf("%s\t use %dm\t %s\n", step, memory, elapse);
        }
    }

}
