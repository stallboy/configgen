package configgen.data;

import configgen.CSV;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Rules {

    private static Pattern intPostfixPattern = Pattern.compile("(.*\\D)(\\d+)");

    enum SepType {
        None, IntPostfix, BeanPrefix
    }
    static class Sep {
        SepType type = SepType.None;
        String field;
        int num;
    }

    static Sep trySep(String name) {
        Sep r = new Sep();
        int i = name.indexOf('@');
        if (i != -1) {
            r.type = SepType.BeanPrefix;
            r.field = name.substring(0, i);
        } else {
            Matcher m = intPostfixPattern.matcher(name);
            if (m.matches()) {
                r.type = SepType.IntPostfix;
                r.field = m.group(1);
                r.num = Integer.parseInt(m.group(2));
            }
        }
        return r;
    }

    static String makeListName(String name) {
        return name + "List";
    }

    private static Pattern listpattern = Pattern.compile("(\\D.*)List");

    static String parseListName(String name) {
        Matcher m = listpattern.matcher(name);
        if (m.matches())
            return m.group(1);
        throw new RuntimeException("list name not endswith List£º " + name);
    }

    static String makeMapName(String key, String value) {
        return key + "2" + value + "Map";
    }

    private static Pattern mappattern = Pattern.compile("(.*\\D)2(\\D.*)Map");

    static class Pair {
        String key;
        String value;
    }

    static Pair parseMapName(String name) {
        Pair r = new Pair();
        Matcher m = mappattern.matcher(name);
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

    static String guessPrimitiveTypeOrList(Set<String> data){
        String t = guessPrimitiveType(data);
        if (t.equals("string")){
            Collection<String> parsed = new ArrayList<>();
            for (String s : data)
                parsed.addAll(CSV.parseList(s));
            if (parsed.size() > data.size() * 1.8) {
                return "list,"+ guessPrimitiveType(new HashSet<>(parsed));
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
                CSV.parseInt(s);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLong(Set<String> data) {
        for (String s : data) {
            try {
                CSV.parseLong(s);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFloat(Set<String> data) {
        for (String s : data) {
            try {
                CSV.parseFloat(s);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

}
