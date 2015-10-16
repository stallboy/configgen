package configgen;

import configgen.data.Datas;
import configgen.define.ConfigCollection;
import configgen.gen.Generator;
import configgen.type.Cfgs;
import configgen.value.CfgVs;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class Main {
    private static void usage(String reason) {
        System.err.println(reason);

        System.out.println("Usage: java -jar configgen.jar [options]");
        System.out.println("	-configdir  config data directory.");
        System.out.println("	-xml        default config.xml in datadir.");
        System.out.println("	-encoding   csv and xml encoding. default GBK");
        System.out.println("	-v          verbose, default no");
        Generator.providers.forEach((k, v) -> System.out.println("	-gen        " + k + "," + v.usage()));
        Runtime.getRuntime().exit(1);
    }

    public static void main(String[] args) throws Exception {
        String configdir = null;
        String xml = null;
        String encoding = "GBK";
        List<Generator> generators = new ArrayList<>();

        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-configdir":
                    configdir = args[++i];
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
                default:
                    usage("unknown args " + args[i]);
                    break;
            }
        }

        if (configdir == null) {
            usage("-configdir miss");
            return;
        }

        Path dir = Paths.get(configdir);
        File xmlFile = xml != null ? new File(xml) : dir.resolve("config.xml").toFile();
        Logger.verbose("parse xml " + xmlFile);
        ConfigCollection define = new ConfigCollection(xmlFile);
        Cfgs type = new Cfgs(define);
        type.resolve();

        Logger.verbose("read data " + dir + " then auto complete xml");
        Datas data = new Datas(dir, encoding);
        data.autoCompleteDefine(type);
        define.save(xmlFile, encoding);
        Cfgs newType = new Cfgs(define);
        newType.resolve();

        Logger.verbose("verify constraint");
        CfgVs value = new CfgVs(newType, data);
        value.verifyConstraint();

        for (Generator generator : generators) {
            Logger.verbose("generate " + generator.context.arg);
            generator.generate(dir, value);
        }

        Logger.verbose("end");
    }
}
