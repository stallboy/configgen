package configgen.gen;

import configgen.define.DomUtils;
import configgen.value.CfgV;
import configgen.value.CfgVs;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenPack extends Generator {
    private File dstDir;
    private String xml;

    public GenPack() {
        providers.put("pack", this);
    }

    @Override
    public String usage() {
        return "dir:cfg    add ,xml:pack.xml if xml not default pack.xml in configdir";
    }

    @Override
    public boolean parse(Context ctx) {
        dstDir = new File(ctx.get("dir", "cfg"));
        xml = ctx.get("xml", null);
        return ctx.end();
    }

    @Override
    public void generate(Path configDir, CfgVs value) throws IOException {
        Map<String, Set<String>> packs = new HashMap<>();
        Set<String> source = new HashSet<>(value.cfgvs.keySet());
        Set<String> picked = new HashSet<>();
        File packXmlFile = xml != null ? new File(xml) : configDir.resolve("pack.xml").toFile();
        Element root = DomUtils.rootElement(packXmlFile);
        for (Element ep : DomUtils.elementsList(root, "pack").get(0)) {
            String[] attributes = DomUtils.attributes(ep, "name", "cfgs");
            String packName = attributes[0].endsWith(".zip") ? attributes[0].substring(0, attributes[0].length() - 4) : attributes[0];
            Assert(!packName.equalsIgnoreCase("text"), "text.zip reserved for i18n");
            Set<String> packCfgs = new HashSet<>();
            packs.put(packName, packCfgs);
            for (String c : attributes[1].split(",")) {
                if (c.equals(".**")) {
                    packCfgs.addAll(source);
                    picked.addAll(source);
                    Assert(source.size() > 0, c + " not exist");
                } else if (c.equals(".*")) {
                    int cnt = 0;
                    for (String n : source) {
                        if (!n.contains(".")) {
                            Assert(picked.add(n), n + " duplicate");
                            packCfgs.add(n);
                            cnt++;
                        }
                    }
                    Assert(cnt > 0, c + " not exist");
                } else if (c.endsWith(".**")) {
                    String prefix = c.substring(0, c.length() - 2);
                    int cnt = 0;
                    for (String n : source) {
                        if (n.startsWith(prefix)) {
                            Assert(picked.add(n), n + " duplicate");
                            packCfgs.add(n);
                            cnt++;
                        }
                    }
                    Assert(cnt > 0, c + " not exist");
                } else if (c.endsWith(".*")) {
                    String prefix = c.substring(0, c.length() - 1);
                    int cnt = 0;
                    for (String n : source) {
                        if (n.startsWith(prefix) && !n.substring(prefix.length()).contains(".")) {
                            Assert(picked.add(n), n + " duplicate");
                            packCfgs.add(n);
                            cnt++;
                        }
                    }
                    Assert(cnt > 0, c + " not exist");
                } else {
                    Assert(picked.add(c), c + " duplicate");
                    packCfgs.add(c);
                    Assert(source.contains(c), c + " not exist");
                }
            }
        }
        source.removeAll(picked);
        Assert(source.isEmpty(), source + " not contained in pack.xml");

        mkdirs(dstDir);
        try (ZipOutputStream textOS = new ZipOutputStream(new CheckedOutputStream(new CachedFileOutputStream(new File(dstDir, "text.zip")), new CRC32()))) {
            ZipEntry tze = new ZipEntry("text.csv");
            tze.setTime(0);
            textOS.putNextEntry(tze);
            for (Map.Entry<String, Set<String>> entry : packs.entrySet()) {
                String packName = entry.getKey();
                Set<String> packCfgs = entry.getValue();
                if (!packCfgs.isEmpty()) {
                    try (ZipOutputStream zos = new ZipOutputStream(new CheckedOutputStream(new CachedFileOutputStream(new File(dstDir, packName + ".zip")), new CRC32()))) {
                        ZipEntry ze = new ZipEntry(packName);
                        ze.setTime(0);
                        zos.putNextEntry(ze);
                        try (ValueOutputStream os = new ValueOutputStream(zos, textOS)) {
                            for (String cfg : packCfgs) {
                                CfgV cv = value.cfgvs.get(cfg);
                                os.addCfgV(cv);
                            }
                        }
                    }
                }
            }
        }
    }

    private void Assert(boolean cond, String... str) {
        if (!cond)
            throw new AssertionError("gen pack: " + String.join(",", str));
    }
}
