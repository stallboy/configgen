package configgen.gen;

import configgen.value.CfgVs;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Context {
    private String arg;
    private String type;
    private Map<String, String> ctx = new HashMap<>();

    public Context(String arg) {
        this.arg = arg;
        String[] sp = arg.split(",");
        type = sp[0];
        for (int i = 1; i < sp.length; i++) {
            String[] c = sp[i].split(":");
            ctx.put(c[0], c[1]);
        }
    }

    public Generator create(Path dir, CfgVs value) {
        switch (type) {
            case "zip":
                return new GenZip(dir, value, this);
            default:
                return null;
        }
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
