package configgen.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CSVParser {

    private static final char comma = ',';
    private static final char quote = '"';
    private static final char cr = '\r';
    private static final char lf = '\n';

    private enum State {
        START, NO_QUOTE, QUOTE, QUOTE2, CR,
    }


    /**
     * 会先检查path对应的文件是否有bom头，如果有就用bom头里的信息（比如识别出来时utf8）来作为文件编码
     * 如果没有bom头，则就用这里的参数encoding来打开文件。
     * 好处是：一般的中文文件就都是GBK编码，
     * 如果国际化成泰语，GBK不行了，需要utf8，则只要此csv文件有bom头就ok
     */
    public static List<List<String>> readFromFile(Path path, String encoding) throws IOException {
        byte[] buf = Files.readAllBytes(path);
        int nread = buf.length;

        BomChecker.Res bom = BomChecker.checkBom(buf, nread, encoding);
        String fileStr = new String(buf, bom.bomSize, nread - bom.bomSize, bom.encoding);
        return parse(fileStr);
    }

    //https://tools.ietf.org/html/rfc4180
    public static List<List<String>> parse(String source) {

        final StringBuilder field = new StringBuilder(128);


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
                            field.append(c); //忽略了"，
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
        String s = field.toString();

        if (s.length() < 5) {//与速度和内存间取个平衡吧
            s = s.intern();
        }
        record.add(s);
    }

    private static void addRecord(ArrayList<List<String>> result, List<String> record) {
        if (checkRecordEmpty(record)) {
            record = Collections.emptyList(); //作为空行标记
        }

        result.add(record);
    }

    public static boolean checkRecordEmpty(List<String> record) {
        for (String s : record) {
            if (!s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
