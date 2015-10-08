package configgen;

import configgen.data.Datas;
import configgen.define.ConfigCollection;
import configgen.gen.*;
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
        Context.providers.forEach((k, v) -> System.out.println("	-gen        " + v));
        Runtime.getRuntime().exit(1);
    }

    public static void main(String[] args) throws Exception {
        String configdir = null;
        String xml = null;
        String encoding = "GBK";
        List<Context> contexts = new ArrayList<>();

        new GenBin();
        new GenZip();
        new GenJava();
        new GenCs();

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
                    contexts.add(new Context(args[++i]));
                    break;
                case "-v":
                    Logger.enableVerbose(true);
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


        for (Context ctx : contexts) {
            Generator g = ctx.create();
            if (g != null) {
                Logger.verbose("generate " + ctx);
                g.generate(dir, value, ctx);
            } else {
                System.err.println("not support " + ctx);
            }
        }

        Logger.verbose("end");
    }
}
