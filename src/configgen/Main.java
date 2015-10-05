package configgen;

import configgen.data.Datas;
import configgen.define.ConfigCollection;
import configgen.gen.Context;
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
        System.out.println("	-configdir       config data directory. no default");
        System.out.println("	-encoding        config encoding. default GBK");
        System.out.println("	-gen             zip,bin,java,cs,lua");
        System.out.println("	-v               verbose, default false");
        Runtime.getRuntime().exit(1);
    }

    public static void main(String[] args) throws Exception {
        String configDir = null;
        String encoding = "GBK";
        List<Context> contexts = new ArrayList<>();

        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-configdir":
                    configDir = args[++i];
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

        if (configDir == null) {
            usage("-configdir miss");
            return;
        }


        Path dir = Paths.get(configDir);
        File xml = dir.resolve("config.xml").toFile();
        Logger.verbose("parse xml to define");
        ConfigCollection define = new ConfigCollection(xml);

        Logger.verbose("resolve define to type");
        Cfgs type = new Cfgs(define);
        type.resolve();

        Logger.verbose("read and analyze data to refine define");
        Datas data = new Datas(dir, encoding);
        data.refineDefine(type);

        Logger.verbose("save to xml");
        define.save(xml, encoding);

        Logger.verbose("resolve refined define to new type");
        Cfgs newType = new Cfgs(define);
        newType.resolve();

        Logger.verbose("construct value from new type and data");
        CfgVs value = new CfgVs(newType, data);

        Logger.verbose("verify value constraint");
        value.verifyConstraint();

        for (Context ctx : contexts) {
            Generator g = ctx.create(dir, value);
            if (g != null) {
                Logger.verbose("generate " + ctx);
                g.gen();
            } else {
                System.err.println("not support " + ctx);
            }
        }

        Logger.verbose("end");
    }
}
