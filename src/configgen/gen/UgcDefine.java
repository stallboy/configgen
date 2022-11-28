package configgen.gen;

import configgen.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UgcDefine {

    private static final String ugcTxtFile = "ugc.txt";

    private final Set<String> ugcTableNames = new HashSet<>();

    public UgcDefine(Path dataDir, String encoding) {
        Path fn = dataDir.resolve(ugcTxtFile).normalize();
        if (Files.exists(fn)) {
            try {
                List<String> lines = Files.readAllLines(fn, Charset.forName(encoding));
                ugcTableNames.addAll(lines);
            } catch (IOException e) {
                Logger.log(String.format("以编码%s读%s文件异常, 忽略此文件", encoding, fn.toAbsolutePath()));
            }
        }
    }

    public boolean isUgcTable(String tableName) {
        return ugcTableNames.contains(tableName);
    }

}
