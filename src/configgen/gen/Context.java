package configgen.gen;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Context {
    private String arg;
    private String type;
    private Map<String, String> ctx = new HashMap<>();

    public static final Map<String, String> providers = new LinkedHashMap<>();

    public Context(String arg) {
        this.arg = arg;
        String[] sp = arg.split(",");
        type = sp[0];
        for (int i = 1; i < sp.length; i++) {
            String s = sp[i];
            int c = s.indexOf(':');
            if (-1 == c)
                ctx.put(s, null);
            else
                ctx.put(s.substring(0, c), s.substring(c + 1));
        }
    }

    public Generator create() {
        return Generator.providers.get(type);
    }

    public String get(String key, String def) {
        String v = ctx.remove(key);
        return v != null ? v : def;
    }

    public void end() {
        if (!ctx.isEmpty())
            System.err.println("-gen " + type + " not support " + ctx);
    }

    @Override
    public String toString() {
        return arg;
    }
}
