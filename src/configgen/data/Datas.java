package configgen.data;

import configgen.CSV;
import configgen.Node;
import configgen.define.Config;
import configgen.define.ConfigCollection;
import configgen.type.Cfg;
import configgen.type.Cfgs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class Datas extends Node {
    public final Map<String, Data> datas = new HashMap<>();

    public Datas(Path dataDir, String inputEncoding) throws IOException {
        super(null, "data");
        Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes a) throws IOException {
                if (file.toString().endsWith(".csv")) {
                    String name = path2ConfigName(dataDir.relativize(file).toString());
                    try (Reader reader = new InputStreamReader(new FileInputStream(file.toFile()), inputEncoding)) {
                        datas.put(name, new Data(Datas.this, name, CSV.parse(reader, false)));
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static String path2ConfigName(String p) {
        String[] res = p.split("\\\\|/");
        String last = res[res.length - 1];
        res[res.length - 1] = last.substring(0, last.length() - 4);
        return String.join(".", res).toLowerCase();
    }

    public void refineDefine(Cfgs cfgs) {
        ConfigCollection define = cfgs.define;
        Map<String, Cfg> old = new HashMap<>(cfgs.cfgs);
        define.configs.clear();
        datas.forEach((k, data) -> {
            Cfg cfg = old.remove(k);
            Config def;
            if (cfg != null) {
                def = cfg.define;
            } else {
                def = new Config(define, k);
            }
            define.configs.put(k, def);
            data.parse(cfg);
            data.refineDefine(def);
        });
    }
}
