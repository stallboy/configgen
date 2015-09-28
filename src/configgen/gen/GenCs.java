package configgen.gen;

import configgen.value.CfgVs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GenCs extends Generator {
    private File dstDir;
    private String pkg;
    private String encoding;

    public GenCs(Path dir, CfgVs value, Context ctx) {
        super(dir, value, ctx);
        String _dir = ctx.get("dir", ".");
        pkg = ctx.get("pkg", "Config");
        encoding = ctx.get("encoding", "GBK");
        ctx.end();
        dstDir = Paths.get(_dir).resolve(pkg.replace('.', '/')).toFile();
    }

    @Override
    public void gen() throws IOException {

    }
}
