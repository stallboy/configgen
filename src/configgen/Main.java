package configgen;

import configgen.define.ConfigCollection;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class Main {

    public static String inputEncoding = "GBK";
    private static String outputEncoding = "GBK";

    static boolean isCSharp;
    static String tool;
    static String codePackage = "config";
    static String own;
    static boolean prefixData;
    private static boolean verbose;

    static PrintStream outputPs(Path path) throws IOException {
        return new PrintStream(new CachedFileOutputStream(path.toFile()), false, outputEncoding);
    }

    static boolean _ISOWN() {
        return own == null;
    }

    private final static SimpleDateFormat df = new SimpleDateFormat("HH.mm.ss.SSS");
    public static void verbose(String s) {
        if (verbose) {
            System.out.println(df.format(Calendar.getInstance().getTime()) + ": " + s);
        }
    }

    private static void usage(String reason) {
        System.out.println(reason);

        System.out.println("Usage: java -jar config.jar [options]");
        System.out.println("	-csharp          generate csharp. default false");
        System.out.println("	-tool            generate csharp code for tool. default null");
        System.out.println("	-dataonly        generate dataonly. default false");
        System.out.println("	-configdir       config data directory. no default");
        System.out.println("	-configxml       config xml file. default config.xml");
        System.out.println("	-codedir         output code directory. default config; csharpconfig");
        System.out.println("	-codepackage     code package name. default config");
        System.out.println("	-datafile        output data file. default configdata.zip; csv.byte,csv.text");
        System.out.println("	-own             set on config, field to generate part of config. default null means all part");
        System.out.println("	-outputencoding  output encoding. default GBK");
        System.out.println("	-inputencoding   input encoding. default GBK");
        System.out.println("	-prefixdata      prefix Data for csharp, default false");
        System.out.println("	-v               verbose, default false");

        Runtime.getRuntime().exit(1);
    }

    public static void main(String[] args) throws Exception {
        boolean dataOnly = false;
        String configDir = null;
        String configXml = "config.xml";
        String codeDir = null;
        String dataFile = "configdata.zip";

        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-csharp":
                    isCSharp = true;
                    break;
                case "-dataonly":
                    dataOnly = true;
                    break;
                case "-tool":
                    tool = args[++i];
                    isCSharp = true;
                    break;
                case "-configdir":
                    configDir = args[++i];
                    break;
                case "-configxml":
                    configXml = args[++i];
                    break;
                case "-codedir":
                    codeDir = args[++i];
                    break;
                case "-codepackage":
                    codePackage = args[++i];
                    break;
                case "-datafile":
                    dataFile = args[++i];
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
                case "-prefixdata":
                    prefixData = true;
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

        if (!isCSharp && own != null)
            usage("-own must not set for java");

        if (tool != null && own != null)
            usage("-own must not set for tool");

        if (isCSharp && !codePackage.equals("config"))
            usage("-codepackage must not set for csharp");

        if (isCSharp)
            codePackage = "Config";

        codeDir = (codeDir == null ? (isCSharp ? "csharpconfig" : "config") : codeDir);

        ConfigCollection configs = new ConfigCollection(Paths.get(configDir).resolve(configXml));
        configs.parseData(Paths.get(configDir));
        configs.updateSchema();

        configs.verifyData();
        configs.verifyDataRef();

        if (isCSharp) {
            ToCsharp to = new ToCsharp(configs);
            if (!dataOnly)
                to.generateCode(Paths.get(codeDir));
            to.generateData(new File(dataFile));
        } else {
            ToJava to = new ToJava(configs, Paths.get(configDir));
            if (!dataOnly)
                to.generateCode(Paths.get(codeDir), codePackage);
            to.generateData(new File(dataFile));
        }
    }
}
