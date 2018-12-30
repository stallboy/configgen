package configgen.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public final class CSV {

    private static final char comma = ',';
    private static final char quote = '"';
    private static final char cr = '\r';
    private static final char lf = '\n';

    private enum State {
        START, NO_QUOTE, QUOTE, QUOTE2, CR,
    }

    public static List<List<String>> readFromFile(File file, String encoding) {
        try {
            try (Reader reader = new UnicodeReader(new FileInputStream(file), encoding)) {
                return parse(reader);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int fieldCountPerRecordHint;
    private static ArrayList<String> emptyRecord = new ArrayList<>();

    //https://tools.ietf.org/html/rfc4180
    public static List<List<String>> parse(Reader reader) throws IOException {
        fieldCountPerRecordHint = -1;
        ArrayList<List<String>> result = new ArrayList<>();
        ArrayList<String> record = new ArrayList<>();
        State state = State.START;
        StringBuilder field = null;

        for (int i = reader.read(); i != -1; i = reader.read()) {
            char c = (char) i;

            switch (state) {
                case START:
                    switch (c) {
                        case comma:
                            record.add("");
                            break;
                        case quote:
                            field = new StringBuilder();
                            state = State.QUOTE;
                            break;
                        case cr:
                            field = new StringBuilder();
                            state = State.CR;
                            break;
                        default:
                            field = new StringBuilder();
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

                            record = new ArrayList<>(record.size());
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

    private static void addField(ArrayList<String> record, StringBuilder field){
        String s = field.toString().intern();
        record.add(s);
    }

    private static void addRecord(ArrayList<List<String>> result, ArrayList<String> record){
        if (fieldCountPerRecordHint == -1){
            record.trimToSize();
            fieldCountPerRecordHint = record.size();
        }

        if (!checkRecordHasContent(record)){
            record = emptyRecord;
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

    public static boolean isEmptyRecord(List<String> record){
        return record == emptyRecord;
    }


    private enum ListState {
        START, NO_QUOTE, QUOTE, QUOTE2
    }

    public static List<String> parseList(String str, char separator) {
        ListState state = ListState.START;
        ArrayList<String> list = new ArrayList<>();
        StringBuilder field = null;

        for (char c : str.toCharArray()) {
            switch (state) {
                case START:
                    if (c == separator) {
                        list.add("");
                    } else if (c == quote) {
                        field = new StringBuilder();
                        state = ListState.QUOTE;
                    } else {
                        field = new StringBuilder();
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
