package configgen.gen;

import configgen.value.CfgVs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenZip extends Generator {
    private File dst;

    public GenZip(Path dir, CfgVs value, Context ctx) {
        super(dir, value, ctx);
        dst = new File(ctx.get("file", "configdata.zip"));
        ctx.end();
    }

    @Override
    public void gen() throws IOException {
        try (final ZipOutputStream zos = new ZipOutputStream(new CheckedOutputStream(new CachedFileOutputStream(dst), new CRC32()))) {
            Files.walkFileTree(configDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fn = configDir.relativize(file).toString();
                    if (fn.endsWith(".csv")) {
                        zos.putNextEntry(new ZipEntry(fn));
                        Files.copy(file, zos);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
