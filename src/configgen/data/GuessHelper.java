package configgen.data;

import configgen.util.CSVParser;
import configgen.util.ListParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final class GuessHelper {

    static String getColumnName(String name){
        int i = name.indexOf('@'); //为了是兼容之前版本
        if (i != -1) {
            return name.substring(0, i);
        }else{
            return name;
        }
    }

    static String makeListName(String name) {
        return name + "List";
    }

    static String makeMapName(String key, String value) {
        return key + "2" + value + "Map";
    }

    static String guessPrimitiveType(Set<String> data) {
        if (isInt(data))
            return "int";
        if (isLong(data))
            return "long";
        if (isFloat(data))
            return "float";
        if (isBool(data))
            return "bool";
        return "string";
    }

    static String guessPrimitiveTypeOrList(Set<String> data) {
        String t = guessPrimitiveType(data);
        if (t.equals("string")) {
            Collection<String> parsed = new ArrayList<>();
            for (String s : data)
                parsed.addAll(ListParser.parseList(s, ';'));
            if (parsed.size() > data.size() * 1.8) {
                return "list," + guessPrimitiveType(new HashSet<>(parsed));
            }
        }
        return t;
    }

    private static boolean isBool(Set<String> data) {
        for (String t : data) {
            String s = t.trim();
            if (!s.isEmpty() && !s.equalsIgnoreCase("true") && !s.equalsIgnoreCase("false") && !s.equals("0")
                    && !s.equals("1"))
                return false;
        }
        return true;
    }

    private static boolean isInt(Set<String> data) {
        for (String s : data) {
            try {
                CSVParser.parseInt(s);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLong(Set<String> data) {
        for (String s : data) {
            try {
                CSVParser.parseLong(s);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFloat(Set<String> data) {
        for (String s : data) {
            try {
                CSVParser.parseFloat(s);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}