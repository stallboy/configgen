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

    public GenZip() {
        providers.put("zip", this);
        Context.providers.put("zip", "zip,file:configdata.zip");
    }

    @Override
    public void generate(Path configDir, CfgVs value, Context ctx) throws IOException {
        File dst = new File(ctx.get("file", "configdata.zip"));
        ctx.end();

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
