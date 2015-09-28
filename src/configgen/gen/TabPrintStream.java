package configgen.gen;

import java.io.PrintStream;

public class TabPrintStream {
    private PrintStream ps;


    public TabPrintStream(PrintStream ps) {
        this.ps = ps;
    }

    public void println(String str) {
        ps.println(str);
    }

    public void println() {
        ps.println();
    }

    public void println1(String str) {
        ps.println("    " + str);
    }

    public void println2(String str) {
        ps.println("        " + str);
    }

    public void println3(String str) {
        ps.println("            " + str);
    }

    public void println4(String str) {
        ps.println("                " + str);
    }

    public void println5(String str) {
        ps.println("                    " + str);
    }

}
