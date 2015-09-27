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
import java.util.Calendar;

public final class Main {

    private static String inputEncoding = "GBK";
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
        System.out.println("	-gen             zip,bin,java,csharp,lua");
        System.out.println("	-inputencoding   input encoding. default GBK");
        System.out.println("	-outputencoding  output encoding. default GBK");
        System.out.println("	-v               verbose, default false");
        Runtime.getRuntime().exit(1);
    }

    public static void main(String[] args) throws Exception {
        String configDir = null;
        String gen = null;
        String own = null;

        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-configdir":
                    configDir = args[++i];
                    break;
                case "-gen":
                    gen = args[++i];
                    break;
                case "-own":
                    own = args[++i];
                    break;
                case "-outputencoding":
                    outputEncoding = args[++i];
                    break;
                case "-inputencoding":
                    inputEncoding = args[++i];
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
            usage("-configdir must be set");


        Path dir = Paths.get(configDir);
        File xml = dir.resolve("config.xml").toFile();
        ConfigCollection define = new ConfigCollection(Utils.rootElement(xml));
        //define.dump(System.out);

        Cfgs type = new Cfgs(define);
        //type.dump(System.out);
        type.resolve();
        //type.dump(System.out);

        Datas data = new Datas(dir, inputEncoding);
        //data.dump(System.out);
        data.refineDefine(type);
        define.dump(System.out);

        Document doc = Utils.newDocument();
        define.save(doc);
        Utils.prettySaveDocument(doc, xml, inputEncoding);
        Cfgs newType = new Cfgs(define);

        CfgVs value = new CfgVs(newType, data);
        value.verifyConstraint();

    }
}
