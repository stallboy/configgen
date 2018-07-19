package configgen.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;

public class IndentPrint implements Closeable {
    private final PrintStream ps;
    private int indent;

    public IndentPrint(PrintStream ps, int indent) {
        this.ps = ps;
        this.indent = indent;
        if (indent < 0) {
            throw new IllegalArgumentException("indent < 0");
        }
    }

    public int indent() {
        return indent;
    }

    public void inc() {
        indent++;

    }

    public void dec() {
        indent--;
        if (indent < 0) {
            throw new IllegalArgumentException("indent < 0");
        }
    }

    public void println() {
        ps.print("\n");
    }

    public void println(String fmt, Object... args) {
        printlnn(0, fmt, args);
    }

    public void println1(String fmt, Object... args) {
        printlnn(1, fmt, args);
    }

    public void println2(String fmt, Object... args) {
        printlnn(2, fmt, args);
    }

    public void println3(String fmt, Object... args) {
        printlnn(3, fmt, args);
    }

    public void println4(String fmt, Object... args) {
        printlnn(4, fmt, args);
    }

    public void println5(String fmt, Object... args) {
        printlnn(5, fmt, args);
    }

    public void println6(String fmt, Object... args) {
        printlnn(6, fmt, args);
    }

    public void println7(String fmt, Object... args) {
        printlnn(7, fmt, args);
    }

    private void printlnn(int n, String fmt, Object... args) {
        indent += n;
        if (args.length > 0){
            ps.printf(prefix() + fmt + "\n", args);

        }else{
            ps.print(prefix() + fmt + "\n");
        }
        indent -= n;
    }

    private String prefix() {
        StringBuilder indentStr = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            indentStr.append("    ");
        }
        return indentStr.toString();
    }

    @Override
    public void close() throws IOException {
        ps.close();
    }
}

