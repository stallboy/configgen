package configgen;

import configgen.data.Datas;
import configgen.define.ConfigCollection;
import configgen.gen.CachedFileOutputStream;
import configgen.type.Cfgs;
import configgen.value.CfgVs;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Main {
    private static String outputEncoding = "GBK";
    private static boolean verbose;

    static PrintStream outputPs(Path path) throws IOException {
        return new PrintStream(new CachedFileOutputStream(path.toFile()), false, outputEncoding);
    }

    private final static SimpleDateFormat df = new SimpleDateFormat("HH.mm.ss.SSS");

    public static void verbose(String s) {
        if (verbose) {
            System.out.println(df.format(Calendar.getInstance().getTime()) + ": " + s);
        }
    }

    private static void usage(String reason) {
        System.out.println(reason);

        System.out.println("Usage: java -jar configgen.jar [options]");
        System.out.println("	-configdir       config data directory. no default");
        System.out.println("	-encoding        config encoding. default GBK");
        System.out.println("	-gen             zip,bin,java,cs,lua");
        System.out.println("	-v               verbose, default false");
        Runtime.getRuntime().exit(1);
    }

    static class Gen {
        String type;
        Map<String, String> ctx = new HashMap<>();

        Gen(String param) {
            String[] sp = param.split(",");
            type = sp[0];
            for (int i = 1; i < sp.length; i++) {
                String[] c = sp[i].split(":");
                ctx.put(c[0], c[1]);
            }
        }

    }

    public static void main(String[] args) throws Exception {
        String configDir = null;
        String encoding = "GBK";
        List<Gen> gens = new ArrayList<>();

        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-configdir":
                    configDir = args[++i];
                    break;
                case "-gen":
                    gens.add(new Gen(args[++i]));
                    break;
                case "-encoding":
                    encoding = args[++i];
                    break;
                case "-v":
                    verbose = true;
                    break;
                default:
                    usage("unknown args " + args[i]);
                    break;
            }
        }

        if (configDir == null)
            usage("-configdir miss");


        Path dir = Paths.get(configDir);
        File xml = dir.resolve("config.xml").toFile();
        verbose("parse xml to define");
        ConfigCollection define = new ConfigCollection(Utils.rootElement(xml));

        verbose("resolve define to type");
        Cfgs type = new Cfgs(define);
        type.resolve();

        verbose("parse data to refine define and save to xml");
        Datas data = new Datas(dir, encoding);
        data.refineDefine(type);

        Document doc = Utils.newDocument();
        define.save(doc);
        Utils.prettySaveDocument(doc, xml, encoding);

        verbose("resolve refined define to new type");
        Cfgs newType = new Cfgs(define);
        newType.resolve();

        verbose("construct value from new type and data");
        CfgVs value = new CfgVs(newType, data);

        verbose("verify value of foreign key and range constraint");
        value.verifyConstraint();

        verbose("end");
    }
}
