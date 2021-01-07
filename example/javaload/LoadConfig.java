
import config.ConfigCodeSchema;
import config.ConfigMgr;
import config.ConfigMgrLoader;
import configgen.genjava.ConfigInput;
import configgen.genjava.Schema;
import configgen.genjava.SchemaCompatibleException;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LoadConfig {

    private static final Logger logger = Logger.getLogger("LoadConfig");

    public static void load(String configdata) throws IOException {
        Schema codeSchema = ConfigCodeSchema.getCodeSchema();
        try (ConfigInput input = new ConfigInput(new DataInputStream(new BufferedInputStream(new FileInputStream(configdata))))) {
            Schema dataSchema = Schema.create(input);
            boolean compatible = codeSchema.compatible(dataSchema);
            if (compatible) {
                ConfigMgr mgr = ConfigMgrLoader.load(input);
                ConfigMgr.setMgr(mgr);
            } else {
                throw new SchemaCompatibleException("schema not compatible, ignore load configdata");
            }
        }
    }

    public static void autoReload(ScheduledExecutorService executorService, String configdata, Runnable afterReload) throws IOException {
        listen(executorService, Paths.get(configdata), () -> {
            try {
                load(configdata);
                if (afterReload != null) {
                    afterReload.run();
                }
            } catch (Exception ignored) {
            }
        });
    }

    public static void listen(ScheduledExecutorService executorService, Path path, Runnable callback) throws IOException {
        if (!path.toFile().isDirectory()) { // 简化
            path = path.getParent();
        }
        Path watchPath = path;
        logger.info("start listen " + watchPath.toFile().getCanonicalPath());
        WatchService ws = watchPath.getFileSystem().newWatchService();
        watchPath.register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
        executorService.scheduleWithFixedDelay(() -> {
            WatchKey key = ws.poll();
            if (key != null) {

                StringBuilder sb = new StringBuilder();
                key.pollEvents().forEach(e ->
                        sb.append("context=").append(e.context()).append(",kind=").append(e.kind()).append(";"));
                logger.info("auto reload " + watchPath + " " + sb.toString());
                callback.run();
                key.reset();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws IOException {
        String fn = "example/config.data";
        load(fn);
        autoReload(Executors.newSingleThreadScheduledExecutor(), fn, null);

        System.out.println("ok");
    }
}
