package configgen.gen;

import configgen.value.CfgV;
import configgen.value.CfgVs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenBin extends Generator {
    private File dstDir;
    private String zip;

    public GenBin() {
        providers.put("bin", this);
    }

    @Override
    public String usage() {
        return "dir:.    add ,zip:configdata.zip if need";
    }

    @Override
    public boolean parse(Context ctx) {
        dstDir = new File(ctx.get("dir", "."));
        zip = ctx.get("zip", null);
        return ctx.end();
    }

    @Override
    public void generate(Path configDir, CfgVs value) throws IOException {
        File byteFile = new File(dstDir, "csv.byte");
        File textFile = new File(dstDir, "text.csv");
        try (UTF8Writer texter = new UTF8Writer(new CachedFileOutputStream(textFile)); ValueOutputStream os = new ValueOutputStream(new CachedFileOutputStream(byteFile), texter)) {
            for (CfgV v : value.cfgvs.values()) {
                os.addCfgV(v);
            }
        }

        if (zip != null) {
            try (ZipOutputStream zos = new ZipOutputStream(new CheckedOutputStream(new CachedFileOutputStream(new File(dstDir, zip)), new CRC32()))) {
                ZipEntry ze = new ZipEntry("csv.byte");
                ze.setTime(0);
                zos.putNextEntry(ze);
                Files.copy(byteFile.toPath(), zos);

                ze = new ZipEntry("text.csv");
                ze.setTime(0);
                zos.putNextEntry(ze);
                Files.copy(textFile.toPath(), zos);
            }
            delete(byteFile);
            delete(textFile);
        }
    }
}
