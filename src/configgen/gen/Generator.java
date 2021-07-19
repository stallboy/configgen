package configgen.gen;

import configgen.view.ViewFilter;
import configgen.util.CachedFileOutputStream;
import configgen.util.CachedIndentPrinter;
import configgen.view.OwnFilter;
import configgen.view.XmlBasedFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;

public abstract class Generator {
    protected final Parameter parameter;
    protected final ViewFilter filter;

    /**
     * @param parameter 此接口有2个实现类，一个用于收集usage，一个用于实际参数解析
     *                  从而实现在各Generator的参数需求，只在构造函数里写一次就ok
     */
    protected Generator(Parameter parameter) {
        this.parameter = parameter;
        String viewXml = parameter.get("viewXml", null,"根据定义的视图xml生成数据");
        if (viewXml != null) {
            filter = new XmlBasedFilter(Paths.get(viewXml));
            String own = parameter.get("own", null, "提取部分配置");
            require(own == null, "有viewXml的情况下，不需要再配置own参数");
        } else {
            String own = parameter.get("own", null, "提取部分配置");
            filter = new OwnFilter(own);
        }
    }

    public ViewFilter getFilter() {
        return filter;
    }

    public abstract void generate(Context ctx) throws IOException;


    protected void require(boolean cond, String... str) {
        if (!cond)
            throw new AssertionError(getClass().getSimpleName() + ": " + String.join(",", str));
    }


    protected static CachedIndentPrinter createCode(File file, String encoding) {
        return new CachedIndentPrinter(file, encoding);
    }

    protected static CachedIndentPrinter createCode(File file, String encoding, StringBuilder dst, StringBuilder cache, StringBuilder tmp) {
        return new CachedIndentPrinter(file, encoding, dst, cache, tmp);
    }

    protected static ZipOutputStream createZip(File file) {
        return new ZipOutputStream(new CheckedOutputStream(new CachedFileOutputStream(file), new CRC32()));
    }

    protected static OutputStreamWriter createUtf8Writer(File file) {
        return new OutputStreamWriter(new CachedFileOutputStream(file), StandardCharsets.UTF_8);
    }

    protected static void copyFile(Path dstDir, String file, String dstEncoding) throws IOException {
        try (InputStream is = Generator.class.getResourceAsStream("/support/" + file);
             BufferedReader br = new BufferedReader(new InputStreamReader(is != null ? is : new FileInputStream("src/support/" + file), StandardCharsets.UTF_8));
             CachedIndentPrinter ps = createCode(dstDir.resolve(file).toFile(), dstEncoding)) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                ps.println(line);
            }
        }
    }

    public static String upper1(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    public static String upper1Only(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    public static String lower1(String value) {
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }


}
