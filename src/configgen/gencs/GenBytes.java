package configgen.gencs;

import configgen.gen.Context;
import configgen.gen.Generator;
import configgen.gen.Parameter;
import configgen.util.CachedFileOutputStream;
import configgen.value.AllValue;

import java.io.File;
import java.io.IOException;

public class GenBytes extends Generator {

    private final File file;

    public GenBytes(Parameter parameter) {
        super(parameter);
        file = new File(parameter.get("file", "config.bytes", "文件名"));
        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        AllValue value = ctx.makeValue(filter);

        try (CachedFileOutputStream stream = new CachedFileOutputStream(file, 2048 * 1024)) {
            PackValueVisitor pack = new PackValueVisitor(stream);
            for (String cfg : value.getTableNames()) {
                pack.addVTable(value.getVTable(cfg));
            }
        }
    }
}
