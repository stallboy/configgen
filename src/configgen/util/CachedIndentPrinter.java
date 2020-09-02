package configgen.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CachedIndentPrinter implements Closeable {
    private final Path path;
    private final String encoding;
    private final StringBuilder dst;
    private final StringBuilder cache;
    private final List<StringBuilder> extraCaches = new ArrayList<>();
    private final StringBuilder tmp;
    private int indent;
    private boolean usingCache;
    private int lineCntPerFile;
    private int lineCnt;

    public CachedIndentPrinter(File file, String encoding) {
        this.path = file.toPath().toAbsolutePath().normalize();
        this.encoding = encoding;
        this.dst = new StringBuilder(1024 * 2048);
        this.cache = new StringBuilder(512 * 1024);
        this.tmp = new StringBuilder(128);
        dst.setLength(0);
    }

    public CachedIndentPrinter(File file, String encoding, StringBuilder dst, StringBuilder cache, StringBuilder tmp) {
        this.path = file.toPath().toAbsolutePath().normalize();
        this.encoding = encoding;
        this.dst = dst;
        this.cache = cache;
        this.tmp = tmp;
        dst.setLength(0);
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

    public int enableCache(int extraSplit_LinePerFile, int allLineCnt) {
        usingCache = true;
        cache.setLength(0);
        extraCaches.clear();
        this.lineCntPerFile = extraSplit_LinePerFile;
        this.lineCnt = 0;

        if (extraSplit_LinePerFile > 0 && allLineCnt > 0) {
            int fileCnt = (allLineCnt + extraSplit_LinePerFile - 1) / extraSplit_LinePerFile;
            for (int i = 1; i < fileCnt; i++) {
                extraCaches.add(new StringBuilder());
            }
        }
        return extraCaches.size();
    }

    public void disableCache() {
        usingCache = false;
    }

    public void printCache() {
        dst.append(cache);
    }

    public void printExtraCacheTo(CachedIndentPrinter newPs, int extraIdx) {
        newPs.dst.append(extraCaches.get(extraIdx));
    }

    public void println() {
        to().append("\n");
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
        StringBuilder to = to();
        indent += n;
        if (args.length > 0) {
            tmp.setLength(0);
            prefix(tmp, fmt);
            to.append(String.format(tmp.toString(), args));
        } else {
            prefix(to, fmt);
        }
        indent -= n;
    }

    private StringBuilder to() {
        if (!usingCache) {
            return dst;
        }

        if (extraCaches.isEmpty() || lineCntPerFile <= 0) {
            return cache;
        }


        int idx = lineCnt / lineCntPerFile;
        lineCnt++;

        if (idx == 0) {
            return cache;
        } else {
            idx = Math.min(idx, extraCaches.size());
            return extraCaches.get(idx-1);
        }
    }

    private void prefix(StringBuilder sb, String fmt) {
        for (int i = 0; i < indent; i++) {
            sb.append("    ");
        }
        sb.append(fmt);
        sb.append('\n');
    }

    @Override
    public void close() throws IOException {
        CachedFiles.writeFile(path, dst.toString().getBytes(encoding));
    }
}

