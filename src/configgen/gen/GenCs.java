package configgen.gen;

import configgen.type.Cfg;
import configgen.type.TBean;
import configgen.value.CfgVs;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GenCs extends Generator {
    private File dstDir;
    private String encoding;
    private String prefix;

    public GenCs(Path dir, CfgVs value, Context ctx) {
        super(dir, value, ctx);
        String _dir = ctx.get("dir", ".");
        encoding = ctx.get("encoding", "GBK");
        prefix = ctx.get("prefix", "Data");
        String own = ctx.get("own", null);
        ctx.end();
        dstDir = Paths.get(_dir).resolve("Config").toFile();
        applyOwn(own);
    }

    @Override
    public void gen() throws IOException {
        CachedFileOutputStream.removeOtherFiles(dstDir);
        mkdirs(dstDir);

        copyFile("CSV.cs");
        copyFile("CSVLoader.cs");
        copyFile("LoadErrors.cs");
        copyFile("KeyedList.cs");
        genCSVLoaderDoLoad();

        for (TBean b : value.type.tbeans.values()) {
            genBean(b, null);
        }
        for (Cfg c : value.type.cfgs.values()) {
            genBean(c.tbean, c);
        }

        CachedFileOutputStream.doRemoveFiles();
    }

    private void genBean(TBean tbean, Cfg cfg) {
    }

    private void copyFile(String file) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/support/" + file);
             BufferedReader br = new BufferedReader(new InputStreamReader(is != null ? is : new FileInputStream("src/support/" + file), "GBK"));
             PrintStream ps = cachedPrintStream(new File(dstDir, file), encoding)) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                ps.println(line);
            }
        }
    }

    private void genCSVLoaderDoLoad() throws IOException {
        try (PrintStream stream = cachedPrintStream(new File(dstDir, "CSVLoaderDoLoad.cs"), encoding)) {
            TabPrintStream ps = new TabPrintStream(stream);
            ps.println("using System.Collections.Generic;");
            ps.println("using System.IO;");
            ps.println();

            ps.println("namespace Config");
            ps.println("{");

            ps.println1("public static partial class CSVLoader {");
            ps.println();
            ps.println2("public static LoadErrors DoLoad(BinaryReader byter, Dictionary<string, Dictionary<ushort, string>> allTextMap)");
            ps.println2("{");
            ps.println3("var errors = new LoadErrors();");
            ps.println3("var configNulls = new List<string>");
            ps.println3("{");
            for (String name : value.type.cfgs.keySet()) {
                ps.println4("\"" + name + "\",");
            }
            ps.println3("};");

            ps.println3("for(;;)");
            ps.println3("{");
            ps.println4("try");
            ps.println4("{");
            ps.println5("var csv = CSV.ReadString(byter);");
            ps.println5("var count = byter.ReadUInt16();");
            ps.println5("Dictionary<ushort, string> textMap;");
            ps.println5("allTextMap.TryGetValue(csv, out textMap);");

            ps.println5("switch(csv)");
            ps.println5("{");

            value.type.cfgs.forEach((name, cfg) -> {
                ;
                ps.println6("case \"" + name + "\":");
                ps.println7("configNulls.Remove(csv);");
                //ps.println7(cfg.getBean().FullN() + ".Initialize(count, byter, textMap, errors);");
                ps.println7("break;");
            });

            ps.println6("default:");
            ps.println7("errors.ConfigDataAdd(csv);");
            ps.println7("break;");
            ps.println5("}");

            ps.println4("}");
            ps.println4("catch (EndOfStreamException)");
            ps.println4("{");
            ps.println5("break;");
            ps.println4("}");

            ps.println3("}");

            ps.println3("foreach (var csv in configNulls)");
            ps.println4("errors.ConfigNull(csv);");

            //configs.stream().filter(cfg -> cfg.getBean().HASREF()).forEach(cfg -> ps.println("            " + cfg.getBean().FullN() + ".Resolve(errors);"));

            ps.println3("return errors;");
            ps.println2("}");
            ps.println();
            ps.println1("}");
            ps.println("}");
            ps.println();
        }
    }

}
