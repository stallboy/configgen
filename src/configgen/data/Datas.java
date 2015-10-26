package configgen.data;

import configgen.Logger;
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
    public final Path dataDir;
    public final Map<String, Data> datas = new HashMap<>();

    public Datas(Path _dataDir, String encoding) throws IOException {
        super(null, "data");
        dataDir = _dataDir;
        Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes a) throws IOException {
                String path = dataDir.relativize(file).toString();
                if (path.endsWith(".csv")) {
                    String name = CSV.path2ConfigName(path.substring(0, path.length() - 4));
                    try (Reader reader = encoding.startsWith("UTF") ? new InputStreamReader(new FileInputStream(file.toFile()), encoding) : new UnicodeReader(new FileInputStream(file.toFile()), encoding)) {
                        datas.put(name, new Data(Datas.this, name, CSV.parse(reader, false)));
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void autoCompleteDefine(Cfgs cfgs) {
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
                Logger.verbose("new config " + def.fullName());
            }
            define.configs.put(k, def);
            data.parse(cfg);
            data.autoCompleteDefine(def);
        });

        old.forEach((k, cfg) -> Logger.verbose("delete config " + cfg.fullName()));
    }
}
