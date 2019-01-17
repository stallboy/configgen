package configgen.util;

import java.util.ArrayList;
import java.util.List;

public class ListParser {

    private static StringBuilder field = new StringBuilder(128); //这里假设是单线程

    private enum ListState {
        START, NO_QUOTE, QUOTE, QUOTE2
    }

    private static final char quote = '"';

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
                        list.add(field.toString());
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
                        list.add(field.toString());
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
            case START: //注意这里和CSVParser的不同，这里最后一个分隔符后面如果没有符号，就不算
                break;
            default:
                list.add(field.toString());
                break;
        }
        list.trimToSize();
        return list;
    }


}
