package configgen.gen;

import configgen.value.CfgV;
import configgen.value.CfgVs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenBin extends Generator {

    static void register() {
        providers.put("bin", new Provider() {
            @Override
            public Generator create(Parameter parameter) {
                return new GenBin(parameter);
            }

            @Override
            public String usage() {
                return "dir:.    add ,zip:configdata.zip if need";
            }
        });
    }

    private File dstDir;
    private String zip;

    public GenBin(Parameter parameter) {
        super(parameter);
        dstDir = new File(parameter.get("dir", "."));
        zip = parameter.get("zip", null);
        parameter.end();
    }


    @Override
    public void generate(CfgVs value) throws IOException {
        byte[] bytes;
        byte[] texts;
        try (ByteArrayOutputStream bs = new ByteArrayOutputStream();
             ByteArrayOutputStream ts = new ByteArrayOutputStream();
             UTF8Writer texter = new UTF8Writer(ts);
             ValueOutputStream os = new ValueOutputStream(bs, texter)) {
            for (CfgV v : value.cfgvs.values()) {
                os.addCfgV(v);
            }
            bytes = bs.toByteArray();
            texts = ts.toByteArray();
        }

        if (zip != null) {
            try (ZipOutputStream zos = createZip(new File(dstDir, zip))) {
                ZipEntry ze = new ZipEntry("csv.byte");
                ze.setTime(0);
                zos.putNextEntry(ze);
                zos.write(bytes);

                ze = new ZipEntry("text.csv");
                ze.setTime(0);
                zos.putNextEntry(ze);
                zos.write(texts);
            }
        } else {
            try (CachedFileOutputStream bs = new CachedFileOutputStream(new File(dstDir, "csv.byte"));
                 ByteArrayOutputStream ts = new CachedFileOutputStream(new File(dstDir, "text.csv"))) {
                bs.write(bytes);
                ts.write(texts);
            }
        }
    }
}
