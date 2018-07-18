package configgen.gencs;

import configgen.Logger;
import configgen.util.DomUtils;
import configgen.gen.*;
import configgen.util.CachedFileOutputStream;
import configgen.value.VDb;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenPack extends Generator {

    public static void register() {
        Generators.addProvider("pack", new GeneratorProvider() {
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
    private final int packAll;

    private GenPack(Parameter parameter) {
        super(parameter);
        dstDir = new File(parameter.getNotEmpty("dir", "cfg"));
        xml = parameter.get("xml", null);
        packAll = Integer.parseInt(parameter.get("packall", "0"));
        own = parameter.get("own", null);

        parameter.end();
    }

    @Override
    public void generate(Context ctx) throws IOException {
        VDb value = ctx.makeValue(own);
        Map<String, Set<String>> packs = new HashMap<>();

        if (packAll != 0) {
            packs.put("all", value.vtables.keySet());
        } else {
            File packXmlFile = xml != null ? new File(xml) : ctx.data.dataDir.resolve("pack.xml").toFile();
            if (packXmlFile.exists()) {
                parsePack(packs, packXmlFile, value);
            } else {
                Logger.log(packXmlFile.getCanonicalPath() + "  not exist, pack to all.zip");
                packs.put("all", value.vtables.keySet());
            }
        }


        for (Map.Entry<String, Set<String>> entry : packs.entrySet()) {
            String packName = entry.getKey();
            Set<String> packCfgs = entry.getValue();
            if (!packCfgs.isEmpty()) {
                try (ZipOutputStream zos = createZip(new File(dstDir, packName + ".zip"))) {
                    ZipEntry ze = new ZipEntry(packName);
                    ze.setTime(0);
                    zos.putNextEntry(ze);
                    PackValueVisitor pack = new PackValueVisitor(zos);
                    for (String cfg : packCfgs) {
                        pack.addVTable(value.vtables.get(cfg));
                    }
                }
            }
        }


        try (OutputStreamWriter writer = new OutputStreamWriter(new CachedFileOutputStream(new File(dstDir, "entry.txt")), StandardCharsets.UTF_8)) {
            writer.write(String.join(",", packs.keySet()));
        }

        CachedFileOutputStream.keepMetaAndDeleteOtherFiles(dstDir);
    }

    private void parsePack(Map<String, Set<String>> packs, File packXmlFile, VDb value) {
        Set<String> source = new HashSet<>(value.vtables.keySet());
        Set<String> picked = new HashSet<>();

        Element root = DomUtils.rootElement(packXmlFile);
        DomUtils.permitElements(root, "pack");
        for (Element ep : DomUtils.elements(root, "pack")) {
            DomUtils.permitAttributes(ep, "name", "tables");
            String name = ep.getAttribute("name");
            String packName = name.endsWith(".zip") ? name.substring(0, name.length() - 4) : name;
            require(!packName.equalsIgnoreCase("text"), "text.zip reserved for i18n");
            Set<String> packCfgs = new HashSet<>();

            for (String c : ep.getAttribute("tables").split(",")) {
                if (c.equals(".**")) {
                    packCfgs.addAll(source);
                    picked.addAll(source);
                    if (!source.isEmpty())
                        packs.put(packName, packCfgs);
                } else if (c.equals(".*")) {
                    int cnt = 0;
                    for (String n : source) {
                        if (!n.contains(".")) {
                            require(picked.add(n), n + " duplicate");
                            packCfgs.add(n);
                            cnt++;
                        }
                    }
                    if (cnt > 0)
                        packs.put(packName, packCfgs);
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
                    if (cnt > 0)
                        packs.put(packName, packCfgs);
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
                    if (cnt > 0)
                        packs.put(packName, packCfgs);
                } else {
                    require(picked.add(c), c + " duplicate");
                    packCfgs.add(c);
                    require(source.contains(c), c + " not exist");
                    packs.put(packName, packCfgs);
                }
            }
        }
        source.removeAll(picked);
        require(source.isEmpty(), source + " not contained in pack.xml");
    }
}
