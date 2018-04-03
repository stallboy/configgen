package configgen.data;

import configgen.Logger;
import configgen.Node;
import configgen.define.Table;
import configgen.define.Db;
import configgen.type.TTable;
import configgen.type.TDb;

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

public class DDb extends Node {
    public final Path dataDir;
    public final Map<String, DTable> dtables = new HashMap<>();

    public DDb(Path _dataDir, String encoding) throws IOException {
        super(null, "ddb");
        dataDir = _dataDir;
        Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes a) throws IOException {
                String path = dataDir.relativize(file).toString();
                if (path.endsWith(".csv")) {
                    String name = CSV.path2ConfigName(path.substring(0, path.length() - 4));
                    try (Reader reader = encoding.startsWith("UTF") ? new InputStreamReader(new FileInputStream(file.toFile()), encoding) : new UnicodeReader(new FileInputStream(file.toFile()), encoding)) {
                        dtables.put(name, new DTable(DDb.this, name, CSV.parse(reader, false)));
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void autoCompleteDefine(TDb tdb) {
        Db db = tdb.dbDefine;
        Map<String, TTable> old = new HashMap<>(tdb.ttables);
        db.tables.clear();
        for (DTable dTable : dtables.values()) {
            TTable ttable = old.remove(dTable.name);
            Table tableDefine;
            if (ttable != null) {
                tableDefine = ttable.tableDefine;
                db.tables.put(dTable.name, tableDefine);
            } else {
                tableDefine = db.newTable(dTable.name);
                Logger.verbose("new table " + tableDefine.fullName());
            }

            try {
                dTable.parse(ttable);
                dTable.autoCompleteDefine(tableDefine);
            }catch (Throwable e){
                throw  new AssertionError(dTable.name + ", 根据这个表里的数据来猜测表结构和类型出错，看是不是手动在xml里声明一下", e);
            }
        }

        old.forEach((k, cfg) -> Logger.verbose("delete table " + cfg.fullName()));
    }
}
