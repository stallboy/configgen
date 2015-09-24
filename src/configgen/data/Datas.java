package configgen.data;

import configgen.CSV;
import configgen.Main;
import configgen.Node;
import configgen.Utils;
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
        super(null, "");
        Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes a) throws IOException {
                if (file.toString().endsWith(".csv")) {
                    String name = Utils.path2Name(dataDir.relativize(file).toString());
                    try(Reader reader = new InputStreamReader(new FileInputStream(file.toFile()), inputEncoding)){
                        datas.put(name, new Data(Datas.this, name, CSV.parse(reader)));
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void refineDefine(Cfgs cfgs){
        ConfigCollection define = cfgs.define;
        Map<String, Cfg> old = new HashMap<>(cfgs.cfgs);
        datas.forEach( (k, data) -> {
            Cfg cfg = old.remove(k);
            Config def;
            if (cfg != null){
                def = cfg.define;
            }else{
                def = new Config(define, k);
                define.configs.put(k, def);
            }

            data.parse(cfg);
            data.refineDefine(def);
        });
    }


}
