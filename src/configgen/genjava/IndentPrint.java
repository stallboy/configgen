package configgen.genjava;

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

    public void println(String str, Object... args) {
        ps.printf(prefix() + str, args);
        ps.println();
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

