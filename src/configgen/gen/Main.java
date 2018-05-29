package configgen.gen;

import configgen.Logger;
import configgen.data.DDb;
import configgen.define.Db;
import configgen.type.TDb;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Main {
    private static void usage(String reason) {
        System.err.println(reason);

        System.out.println("Usage: java -jar configgen.jar [options]");
        System.out.println("	-datadir      data directory.");
        System.out.println("	-xml          default config.xml in datadir.");
        System.out.println("	-encoding     csv and xml encoding. default GBK");
        System.out.println("	-v            verbose, default no");
        Generator.providers.forEach((k, v) -> System.out.println("	-gen        " + k + "," + v.usage()));
        System.out.println("	-pack         zip filename");
        System.out.println("	-packtext     for i18n, pack text.csv to text.zip");
        System.out.println("	-packxmls     fromDir,toDir separeted by comma");

        Runtime.getRuntime().exit(1);
    }

    public static void main(String[] args) throws Exception {
        String datadir = null;
        String xml = null;

        String encoding = "GBK";
        String pack = null;
        String packtext = null;
        String packxmls = null;
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
                case "-packxmls":
                    packxmls = args[++i];
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

        if (packxmls != null) {
            String[] packs = packxmls.split(",");
            String fromDir = packs[0];
            String toDir = packs[1];
            Logger.verbose("generate zipped xml from " + fromDir + " to " + toDir);
            Path fromPath = Paths.get(fromDir);
            Set<String> fns = new HashSet<>();
            Files.walkFileTree(fromPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fn = fromPath.relativize(file).toString().toLowerCase();
                    if (fn.endsWith(".xml")) {
                        fns.add(fn);
                        String lastfn = file.getFileName().toString().toLowerCase();
                        try (final ZipOutputStream zos = Generator.createZip(new File(toDir, fn))) {
                            ZipEntry ze = new ZipEntry(lastfn);
                            ze.setTime(0);
                            zos.putNextEntry(ze);
                            Files.copy(file, zos);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            try (OutputStreamWriter writer = new OutputStreamWriter(new CachedFileOutputStream(new File(toDir, "entry.txt")), StandardCharsets.UTF_8)) {
                writer.write(String.join(",", fns));
            }
            CachedFileOutputStream.keepMetaAndDeleteOtherFiles(new File(toDir));
            CachedFileOutputStream.finalExit();
        }

        if (datadir == null) {
            if (packtext == null && packxmls == null) {
                usage("-datadir miss");
            }
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

        mm("start");
        Context ctx;

        {
            Db define = new Db(xmlFile);
            mm("define");

            //define.dump(System.out);
            TDb type = new TDb(define);
            type.resolve();
            mm("type");
            //type.dump(System.out);


            DDb data = new DDb(dir, encoding);
            data.autoCompleteDefine(type);
            define.save(xmlFile, encoding);

            mm("data");

            TDb newType = new TDb(define);
            newType.resolve();
            mm("fixtype");
            ctx = new Context(define, newType, data);
        }

        for (Generator generator : generators) {
            Logger.verbose("generate " + generator.parameter);
            generator.generate(ctx);

            mm("gen " + generator.parameter);
        }

        CachedFileOutputStream.finalExit();
        Logger.verbose("end");
    }

    private static void mm(String step) {
        //Runtime.getRuntime().gc();
        Logger.printf("%s\t use %dm, total %dm\n", step, Runtime.getRuntime().totalMemory() / 1024 / 1024, Runtime.getRuntime().maxMemory() / 1024 / 1024);
    }
}
