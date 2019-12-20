package configgen.gen;

import java.util.HashMap;
import java.util.Map;

public class Parameter {
    private final String arg;
    final String type;
    private final Map<String, String> params = new HashMap<>();

    public Parameter(String arg) {
        this.arg = arg;
        String[] sp = arg.split(",");
        type = sp[0];
        for (int i = 1; i < sp.length; i++) {
            String s = sp[i];
            int c = s.indexOf(':');
            if (-1 == c)
                params.put(s, null);
            else
                params.put(s.substring(0, c), s.substring(c + 1));
        }
    }

    public String get(String key, String def) {
        String v = params.remove(key);
        return v != null ? v : def;
    }

    public boolean has(String key) {
        if (params.containsKey(key)) {
            String v = params.remove(key);
            if (v != null) {
                return Boolean.parseBoolean(v);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public String getNotEmpty(String key, String def) {
        String v = params.remove(key);
        if (v != null && v.isEmpty()) {
            throw new AssertionError("-gen " + type + " " + key + " empty");
        }
        return v != null ? v : def;
    }

    public void end() {
        if (!params.isEmpty()) {
            throw new AssertionError("-gen " + type + " not support parameter: " + params);
        }
    }

    @Override
    public String toString() {
        return arg;
    }
}
