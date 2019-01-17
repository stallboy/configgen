package configgen.data;

import configgen.util.CSVParser;
import configgen.util.ListParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GuessHelper {

    private static final Pattern INT_POSTFIX_PATTERN = Pattern.compile("(.*\\D)(\\d+)");

    enum SepType {
        None, IntPostfix, BeanPrefix
    }

    static class Sep {
        SepType type = SepType.None;
        String columnName;
        int num;
    }

    static Sep trySep(String name) {
        Sep r = new Sep();
        int i = name.indexOf('@');
        if (i != -1) {
            r.type = SepType.BeanPrefix;
            r.columnName = name.substring(0, i);
        } else {
            Matcher m = INT_POSTFIX_PATTERN.matcher(name);
            if (m.matches()) {
                r.type = SepType.IntPostfix;
                r.columnName = m.group(1);
                r.num = Integer.parseInt(m.group(2));
            }
        }
        return r;
    }

    static String makeListName(String name) {
        return name + "List";
    }

    private static final Pattern LIST_PATTERN = Pattern.compile("(\\D.*)List");

    static String parseListName(String name) {
        Matcher m = LIST_PATTERN.matcher(name);
        if (m.matches())
            return m.group(1);
        throw new RuntimeException("list名称没有endsWith List " + name);
    }

    static String makeMapName(String key, String value) {
        return key + "2" + value + "Map";
    }

    private static final Pattern MAP_PATTERN = Pattern.compile("(.*\\D)2(\\D.*)Map");

    static class Pair {
        String key;
        String value;
    }

    static Pair parseMapName(String name) {
        Pair r = new Pair();
        Matcher m = MAP_PATTERN.matcher(name);
        if (m.matches()) {
            r.key = m.group(1);
            r.value = m.group(2);
            return r;
        }
        throw new RuntimeException("map name not match <k>2<v>Map: " + name);
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