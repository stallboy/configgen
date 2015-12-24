package configgen.gen;

import configgen.Logger;
import configgen.define.DomUtils;
import configgen.value.CfgVs;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenPack extends Generator {

    static void register() {
        providers.put("pack", new Provider() {
            @Override
            public Generator create(Parameter parameter) {
                return new GenPack(parameter);
            }

            @Override
            public String usage() {
                return "dir:cfg    default ,xml:pack.xml, if not exist pack.xml, pack to all.zip";
            }
        });
    }

    private final File dstDir;
    private final String xml;
    private final String own;

    public GenPack(Parameter parameter) {
        super(parameter);
        dstDir = new File(parameter.getNotEmpty("dir", "cfg"));
        xml = parameter.get("xml", null);
        own = parameter.get("own", null);
        parameter.end();
    }

    @Override
    public void generate(CfgVs _value) throws IOException {
        File packXmlFile = xml != null ? new File(xml) : _value.data.dataDir.resolve("pack.xml").toFile();
        CfgVs value = own != null ? extract(_value, own) : _value;
        Map<String, Set<String>> packs = new HashMap<>();
        if (packXmlFile.exists()) {
            parsePack(packs, packXmlFile, value);
        } else {
            Logger.log(packXmlFile.getCanonicalPath() + "  not exist, pack to all.zip");
            packs.put("all", value.cfgvs.keySet());
        }

        try (ZipOutputStream textOS = createZip(new File(dstDir, "text.zip"))) {
            ZipEntry tze = new ZipEntry("text.csv");
            tze.setTime(0);
            textOS.putNextEntry(tze);
            try (UTF8Writer texter = new UTF8Writer(textOS)) {
                for (Map.Entry<String, Set<String>> entry : packs.entrySet()) {
                    String packName = entry.getKey();
                    Set<String> packCfgs = entry.getValue();
                    if (!packCfgs.isEmpty()) {
                        try (ZipOutputStream zos = createZip(new File(dstDir, packName + ".zip"))) {
                            ZipEntry ze = new ZipEntry(packName);
                            ze.setTime(0);
                            zos.putNextEntry(ze);
                            try (ValueOutputStream vos = new ValueOutputStream(zos, texter)) {
                                for (String cfg : packCfgs) {
                                    vos.addCfgV(value.cfgvs.get(cfg));
                                }
                            }
                        }
                    }
                }
            }
        }

        CachedFileOutputStream.deleteOtherFiles(dstDir);
    }

    private void parsePack(Map<String, Set<String>> packs, File packXmlFile, CfgVs value) {
        Set<String> source = new HashSet<>(value.cfgvs.keySet());
        Set<String> picked = new HashSet<>();

        Element root = DomUtils.rootElement(packXmlFile);
        for (Element ep : DomUtils.elementsList(root, "pack").get(0)) {
            String[] attributes = DomUtils.attributes(ep, "name", "cfgs");
            String packName = attributes[0].endsWith(".zip") ? attributes[0].substring(0, attributes[0].length() - 4) : attributes[0];
            require(!packName.equalsIgnoreCase("text"), "text.zip reserved for i18n");
            Set<String> packCfgs = new HashSet<>();
            packs.put(packName, packCfgs);
            for (String c : attributes[1].split(",")) {
                if (c.equals(".**")) {
                    packCfgs.addAll(source);
                    picked.addAll(source);
                    require(source.size() > 0, c + " not exist");
                } else if (c.equals(".*")) {
                    int cnt = 0;
                    for (String n : source) {
                        if (!n.contains(".")) {
                            require(picked.add(n), n + " duplicate");
                            packCfgs.add(n);
                            cnt++;
                        }
                    }
                    require(cnt > 0, c + " not exist");
                } else if (c.endsWith(".**")) {
                    String prefix = c.substring(0, c.length() - 2);
                    int cnt = 0;
                    for (String n : source) {
                        if (n.startsWith(prefix)) {
                            require(picked.add(n), n + " duplicate");
                            packCfgs.add(n);
                            cnt++;
                        }
                    }
                    require(cnt > 0, c + " not exist");
                } else if (c.endsWith(".*")) {
                    String prefix = c.substring(0, c.length() - 1);
                    int cnt = 0;
                    for (String n : source) {
                        if (n.startsWith(prefix) && !n.substring(prefix.length()).contains(".")) {
                            require(picked.add(n), n + " duplicate");
                            packCfgs.add(n);
                            cnt++;
                        }
                    }
                    require(cnt > 0, c + " not exist");
                } else {
                    require(picked.add(c), c + " duplicate");
                    packCfgs.add(c);
                    require(source.contains(c), c + " not exist");
                }
            }
        }
        source.removeAll(picked);
        require(source.isEmpty(), source + " not contained in pack.xml");
    }
}
