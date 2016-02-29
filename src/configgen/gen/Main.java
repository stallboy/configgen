package configgen.gen;

import configgen.Logger;
import configgen.data.DDb;
import configgen.define.Db;
import configgen.type.TDb;
import configgen.value.VDb;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Main {
    private static void usage(String reason) {
        System.err.println(reason);

        System.out.println("Usage: java -jar configgen.jar [options]");
        System.out.println("	-datadir    data directory.");
        System.out.println("	-xml        default config.xml in datadir.");
        System.out.println("	-encoding   csv and xml encoding. default GBK");
        System.out.println("	-v          verbose, default no");
        Generator.providers.forEach((k, v) -> System.out.println("	-gen        " + k + "," + v.usage()));
        System.out.println("	-pack       zip filename");
        System.out.println("	-packtext   for i18n, pack text.csv to text.zip");

        Runtime.getRuntime().exit(1);
    }

    public static void main(String[] args) throws Exception {
        String datadir = null;
        String xml = null;
        String encoding = "GBK";
        String pack = null;
        String packtext = null;
        List<Generator> generators = new ArrayList<>();

        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-datadir":
                    datadir = args[++i];
                    break;
                case "-xml":
                    xml = args[++i];
                    break;
                case "-encoding":
                    encoding = args[++i];
                    break;
                case "-gen":
                    Generator generator = Generator.create(args[++i]);
                    if (generator == null)
                        usage("");
                    generators.add(generator);
                    break;
                case "-v":
                    Logger.enableVerbose();
                    break;

                case "-packtext":
                    packtext = args[++i];
                    break;
                case "-pack":
                    pack = args[++i];
                    break;
                default:
                    usage("unknown args " + args[i]);
                    break;
            }
        }

        if (packtext != null) {
            Logger.verbose("generate text.zip");
            try (ZipOutputStream zos = Generator.createZip(new File("text.zip"))) {
                ZipEntry ze = new ZipEntry("text.csv");
                ze.setTime(0);
                zos.putNextEntry(ze);
                Files.copy(Paths.get(packtext), zos);
            }
        }

        if (datadir == null) {
            usage("-configdir miss");
            return;
        }
        Path dir = Paths.get(datadir);

        if (pack != null) {
            Logger.verbose("generate " + pack);
            try (final ZipOutputStream zos = Generator.createZip(new File(pack))) {
                Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String fn = dir.relativize(file).toString();
                        if (fn.endsWith(".csv")) {
                            ZipEntry ze = new ZipEntry(fn);
                            ze.setTime(0);
                            zos.putNextEntry(ze);
                            Files.copy(file, zos);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }

        File xmlFile = xml != null ? new File(xml) : dir.resolve("config.xml").toFile();
        Logger.verbose("parse xml " + xmlFile);
        Db define = new Db(xmlFile);
        //define.dump(System.out);
        TDb type = new TDb(define);
        type.resolve();
        //type.dump(System.out);

        Logger.verbose("read data " + dir + " then auto complete xml");
        DDb data = new DDb(dir, encoding);
        data.autoCompleteDefine(type);
        define.save(xmlFile, encoding);
        TDb newType = new TDb(define);
        newType.resolve();

        Logger.verbose("verify constraint");
        VDb value = new VDb(newType, data);
        value.verifyConstraint();
        //value.dump(System.out);

        for (Generator generator : generators) {
            Logger.verbose("generate " + generator.parameter);
            generator.generate(value);
        }

        Logger.verbose("end");
    }
}
