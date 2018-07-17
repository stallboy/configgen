package configgen.gen;

import configgen.Logger;
import configgen.data.DDb;
import configgen.define.Db;
import configgen.gencs.GenCs;
import configgen.gencs.GenPack;
import configgen.genjava.GenJavaCode;
import configgen.genjava.GenJavaData;
import configgen.genlua.GenI18n;
import configgen.genlua.GenLua;
import configgen.type.TDb;
import configgen.value.I18n;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class Main {
    private static void usage(String reason) {
        System.err.println(reason);

        System.out.println("Usage: java -jar configgen.jar [options]");
        System.out.println("	-datadir      data directory.");
        System.out.println("	-xml          default config.xml in datadir.");
        System.out.println("	-encoding     csv and xml encoding. default GBK");
        System.out.println("	-v            verbose, default no");
        Generator.providers.forEach((k, v) -> System.out.println("	-gen        " + k + "," + v.usage()));

        Runtime.getRuntime().exit(1);
    }

    public static void main(String[] args) throws Exception {
        GenJavaCode.register();
        GenJavaData.register();
        GenLua.register();
        GenI18n.register();
        GenAllRefValues.register();
        GenCs.register();
        GenPack.register();

        String datadir = null;
        String xml = null;
        String encoding = "GBK";
        String i18nfile = null;
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
                case "-i18nfile":
                    i18nfile = args[++i];
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


        if (datadir == null) {
            usage("-datadir miss");
            return;
        }
        Path dataDir = Paths.get(datadir);
        File xmlFile = xml != null ? new File(xml) : dataDir.resolve("config.xml").toFile();


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


            DDb data = new DDb(dataDir, encoding);
            data.autoCompleteDefine(type);
            define.save(xmlFile, encoding);
            mm("data");

            TDb newType = new TDb(define);
            newType.resolve();
            mm("fixtype");
            ctx = new Context(define, newType, data, new I18n(i18nfile));
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
