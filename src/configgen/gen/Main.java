package configgen.gen;

import configgen.Logger;
import configgen.data.DDb;
import configgen.define.Db;
import configgen.type.TDb;
import configgen.value.VDb;

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
        System.out.println("   -checkstable  default stableconfig.xml in datadir");
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
        boolean checkstable = false;
        String stablexml = null;
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
                case "-checkstable":
                    checkstable = true;
                    if (!args[i+1].startsWith("-"))
                        stablexml = args[++i];
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

        if (packxmls != null){
            String[] packs = packxmls.split(",");
            String fromDir = packs[0];
            String toDir = packs[1];
            Logger.verbose("generate zipped xml from " + fromDir + " to " + toDir);
            Path fromPath = Paths.get(fromDir);
            Set<String> fns = new HashSet<>();
            Files.walkFileTree(fromPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fn = fromPath.relativize(file).toString();
                    if (fn.endsWith(".xml")) {
                        fns.add(fn);
                        try (final ZipOutputStream zos = Generator.createZip(new File(toDir, fn))) {
                            ZipEntry ze = new ZipEntry(fn);
                            ze.setTime(0);
                            zos.putNextEntry(ze);
                            Files.copy(file, zos);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            try( OutputStreamWriter writer = new OutputStreamWriter(new CachedFileOutputStream(new File(toDir, "entry.txt")), StandardCharsets.UTF_8)){
                writer.write( String.join(",", fns));
            }
            CachedFileOutputStream.keepMetaAndDeleteOtherFiles(new File(toDir));
            CachedFileOutputStream.finalExit();
        }

        if (datadir == null) {
            if (packtext == null && packxmls == null){
                usage("-configdir miss");
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
        File stableXmlFile = null;
        if (checkstable){
            Logger.verbose("start check");
            stableXmlFile = stablexml != null ? new File(stablexml) : dir.resolve("stableconfig.xml").toFile();
        }

        Logger.verbose("parse xml " + xmlFile);
        Db define = new Db(xmlFile);

        if (checkstable) {
            Db stableDefine = new Db(stableXmlFile);
            define.checkInclude(stableDefine);
        }

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

        CachedFileOutputStream.finalExit();
        Logger.verbose("end");
    }
}
