package configgen.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CSV {

    private static final char comma = ',';
    private static final char quote = '"';
    private static final char cr = '\r';
    private static final char lf = '\n';

    private enum State {
        START, NO_QUOTE, QUOTE, QUOTE2, CR,
    }

    private static byte[] buf = new byte[512 * 1024];

    public static List<List<String>> readFromFile(Path path, String encoding) {
        try {
            //使用reader很费内存
            //Reader reader = new UnicodeReader(new BufferedInputStream(new FileInputStream(file)), encoding)
            int nread = readAllBytes(path);
            BomChecker.EncodingAndBomSize bom = BomChecker.checkBom(buf, nread, encoding);
            String fileStr = new String(buf, bom.bomSize, nread - bom.bomSize, bom.encoding);
            return parse(fileStr);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //下面2个方法，来自Files.readAllBytes
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;
    private static final int BUFFER_SIZE = 8192;

    private static int readAllBytes(Path path) throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(path);
             InputStream in = Channels.newInputStream(sbc)) {
            long size = sbc.size();
            if (size > (long) MAX_BUFFER_SIZE)
                throw new OutOfMemoryError("Required array size too large");

            return read(in, (int) size);
        }
    }

    private static int read(InputStream source, int initialSize) throws IOException {
        int capacity = buf.length;
        int nread = 0;
        int n;
        for (; ; ) {
            // read to EOF which may read more or less than initialSize (eg: file
            // is truncated while we are reading)
            while ((n = source.read(buf, nread, capacity - nread)) > 0)
                nread += n;

            // if last call to source.read() returned -1, we are done
            // otherwise, try to read one more byte; if that failed we're done too
            if (n < 0 || (n = source.read()) < 0)
                break;

            // one more byte was read; need to allocate a larger buffer
            if (capacity <= MAX_BUFFER_SIZE - capacity) {
                capacity = Math.max(capacity << 1, BUFFER_SIZE);
            } else {
                if (capacity == MAX_BUFFER_SIZE)
                    throw new OutOfMemoryError("Required array size too large");
                capacity = MAX_BUFFER_SIZE;
            }
            buf = Arrays.copyOf(buf, capacity);
            buf[nread++] = (byte) n;
        }
        return nread;
    }


    private static ArrayList<String> emptyRecord = new ArrayList<>();
    private static StringBuilder field = new StringBuilder(4096); //这里假设是单线程

    //https://tools.ietf.org/html/rfc4180
    public static List<List<String>> parse(String source) {
        ArrayList<List<String>> result = new ArrayList<>();
        ArrayList<String> record = new ArrayList<>();
        State state = State.START;

        field.setLength(0);

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);

            switch (state) {
                case START:
                    switch (c) {
                        case comma:
                            record.add("");
                            break;
                        case quote:
                            field.setLength(0);
                            state = State.QUOTE;
                            break;
                        case cr:
                            field.setLength(0);
                            state = State.CR;
                            break;
                        default:
                            field.setLength(0);
                            field.append(c);
                            state = State.NO_QUOTE;
                            break;
                    }
                    break;

                case NO_QUOTE:
                    switch (c) {
                        case comma:
                            addField(record, field);
                            state = State.START;
                            break;
                        case cr:
                            state = State.CR;
                            break;
                        default:
                            field.append(c);
                            break;
                    }
                    break;

                case QUOTE:
                    switch (c) {
                        case quote:
                            state = State.QUOTE2;
                            break;
                        default:
                            field.append(c);
                            break;
                    }
                    break;

                case QUOTE2:
                    switch (c) {
                        case comma:
                            addField(record, field);
                            state = State.START;
                            break;
                        case quote:
                            field.append(quote);
                            state = State.QUOTE;
                            break;
                        case cr:
                            state = State.CR;
                            break;
                        default:
                            field.append(c);
                            state = State.NO_QUOTE;
                            break;
                    }
                    break;

                case CR:
                    switch (c) {
                        case comma:
                            field.append(cr);
                            addField(record, field);
                            state = State.START;
                            break;
                        case lf:
                            addField(record, field);
                            addRecord(result, record);

                            record = new ArrayList<>(record.size()); //优化下存储
                            state = State.START;
                            break;
                        default:
                            field.append(cr);
                            field.append(c);
                            state = State.NO_QUOTE;
                            break;
                    }
                    break;
            }
        }

        switch (state) {
            case START:
                if (!record.isEmpty()) {
                    record.add("");
                    addRecord(result, record);

                }
                break;
            case CR:
                field.append(cr);
                addField(record, field);
                addRecord(result, record);
                break;
            default:
                addField(record, field);
                addRecord(result, record);
                break;
        }

        result.trimToSize();
        return result;
    }

    private static void addField(ArrayList<String> record, StringBuilder field) {
        String s = field.toString().intern();
        record.add(s);
    }

    private static void addRecord(ArrayList<List<String>> result, ArrayList<String> record) {
        if (!checkRecordHasContent(record)) {
            record = emptyRecord; //作为空行标记
        }

        result.add(record);
    }

    private static boolean checkRecordHasContent(List<String> record) {
        for (String s : record) {
            if (!s.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmptyRecord(List<String> record) {
        return record == emptyRecord;
    }


    private enum ListState {
        START, NO_QUOTE, QUOTE, QUOTE2
    }

    public static List<String> parseList(String str, char separator) {
        ListState state = ListState.START;
        ArrayList<String> list = new ArrayList<>();
        field.setLength(0);

        for (char c : str.toCharArray()) {
            switch (state) {
                case START:
                    if (c == separator) {
                        list.add("");
                    } else if (c == quote) {
                        field.setLength(0);
                        state = ListState.QUOTE;
                    } else {
                        field.setLength(0);
                        field.append(c);
                        state = ListState.NO_QUOTE;
                    }
                    break;

                case NO_QUOTE:
                    if (c == separator) {
                        addField(list, field);
                        state = ListState.START;
                    } else {
                        field.append(c);

                    }
                    break;

                case QUOTE:
                    if (c == quote) {
                        state = ListState.QUOTE2;
                    } else {
                        field.append(c);
                    }
                    break;

                case QUOTE2:
                    if (c == separator) {
                        addField(list, field);
                        state = ListState.START;
                    } else if (c == quote) {
                        field.append(quote);
                        state = ListState.QUOTE;
                    } else {
                        field.append(c);
                        state = ListState.NO_QUOTE;
                    }
                    break;
            }
        }

        switch (state) {
            case START:
                break;
            default:
                addField(list, field);
                break;
        }
        list.trimToSize();
        return list;
    }

    public static boolean parseBoolean(String s) {
        String t = s.trim();
        return t.equals("1") || t.equalsIgnoreCase("true");
    }

    public static float parseFloat(String s) {
        String t = s.trim();
        return t.isEmpty() ? 0.f : Float.parseFloat(t);
    }

    public static int parseInt(String s) {
        String t = s.trim();
        return t.isEmpty() ? 0 : Integer.decode(t);
    }

    public static long parseLong(String s) {
        String t = s.trim();
        return t.isEmpty() ? 0 : Long.decode(t);
    }

}
