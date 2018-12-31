package configgen.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class UTF8Writer implements Closeable {
    private final OutputStream stream;
    private final OutputStreamWriter writer;
    private boolean touched;
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    public UTF8Writer(OutputStream out) {
        stream = out;
        writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }

    public void write(String str) throws IOException {
        if (!touched) {
            stream.write(UTF8_BOM);
            touched = true;
        }
        writer.write(str);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
