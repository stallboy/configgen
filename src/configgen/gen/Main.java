package configgen.gen;

import configgen.Logger;
import configgen.genallref.GenAllRefValues;
import configgen.gencs.GenCs;
import configgen.gencs.GenPack;
import configgen.genjava.GenJavaCode;
import configgen.genjava.GenJavaData;
import configgen.genlua.GenI18n;
import configgen.genlua.GenLua;
import configgen.util.CachedFileOutputStream;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class Main {
    private static void usage(String reason) {
        System.err.println(reason);

        System.out.println("Usage: java -jar configgen.jar [options]");
        System.out.println("	-datadir      配表所在目录");
        System.out.println("	-xml          配表结构文件，默认是config.xml");
        System.out.println("	-encoding     配表和配表结构文件的编码，默认是GBK");
        System.out.println("	-utf8files    utf8编码的配表文件列表，逗号分割，默认空, 用于encoding不是utf8但有部分文件是utf8编码时");
        System.out.println("	              比如datadir下有task/question.csv是utf8编码，则这里要写成-utf8files task.question");

        System.out.println("	-i18nfile     国际化需要的文件，如果不用国际化，就不要配置");
        System.out.println("	-i18nencoding 国际化需要的文件的编码，默认是GBK");
        System.out.println("   -i18ncrlfaslf     把字符串里的\\r\\n 替换为 \\n，默认是false");
        System.out.println("	-verify       检查配表约束");
        System.out.println("	-v            输出一些额外信息");
        Generators.getAllProviders().forEach((k, v) -> System.out.println("	-gen        " + k + "," + v.usage()));

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
        String utf8files = null;
        String i18nfile = null;
        String i18nencoding = "GBK";
        boolean i18ncrlfaslf = false;
        boolean verify = false;
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
                case "-utf8files":
                    utf8files = args[++i];
                    break;


                case "-i18nfile":
                    i18nfile = args[++i];
                    break;
                case "-i18nencoding":
                    i18nencoding = args[++i];
                    break;
                case "-i18ncrlfaslf":
                    i18ncrlfaslf = true;
                    break;
                case "-verify":
                    verify = true;
                    break;


                case "-gen":
                    Generator generator = Generators.create(args[++i]);
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
            usage("-datadir 未配置");
            return;
        }

        if (i18nfile != null) {
            Logger.enablePrintNotFound18n();
        }

        Path dataDir = Paths.get(datadir);
        File xmlFile = xml != null ? new File(xml) : dataDir.resolve("config.xml").toFile();

        Set<String> utf8fileset;
        if (utf8files != null) {
            utf8fileset = new HashSet<>(Arrays.asList(utf8files.split(",")));
        } else {
            utf8fileset = Collections.emptySet();
        }

        Context ctx = new Context(dataDir, xmlFile, encoding, utf8fileset, i18nfile, i18nencoding, i18ncrlfaslf);
        if (verify) {
            Logger.verbose("verify");
            ctx.verify();
        }

        for (Generator generator : generators) {
            Logger.verbose("generate " + generator.parameter);
            generator.generate(ctx);
        }

        CachedFileOutputStream.finalExit();
        Logger.verbose("end");
    }


}
