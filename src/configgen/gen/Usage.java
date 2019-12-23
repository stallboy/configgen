package configgen.gen;

import java.util.LinkedHashMap;
import java.util.Map;

class Usage implements Parameter {
    private static class Info {
        String def;
        boolean flag;
        String info;

        Info(String def, boolean flag, String info) {
            this.def = def;
            this.flag = flag;
            this.info = info;
        }
    }

    private final Map<String, Info> infos = new LinkedHashMap<>();


    void print() {
        infos.forEach((key, info) -> {
            if (info.flag) {
                System.out.printf("        %s -- %s,默认为false\n", key, info.info);
            } else {
                String def = info.def;
                if (def == null) {
                    def = "null";
                }
                System.out.printf("        %s=%s -- %s\n", key, def, info.info);

            }
        });
    }


    @Override
    public String get(String key, String def, String info) {
        infos.put(key, new Info(def, false, info));
        return def;
    }

    @Override
    public boolean has(String key, String info) {
        infos.put(key, new Info("false", true, info));
        return false;
    }

    @Override
    public void end() {
    }
}
