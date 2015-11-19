package config;

import java.nio.file.Path;
import java.util.Set;
import java.util.LinkedHashSet;

public class CSVLoader {

    public static Set<String> load(Path zipPath, String encoding, boolean reload) throws Exception {
        Set<String> configsInZip = CSV.load(zipPath, encoding, reload);
        Set<String> configsInCode = new LinkedHashSet<>(java.util.Arrays.asList("equip.ability",
            "equip.jewelry",
            "equip.jewelryrandom",
            "equip.jewelrysuit",
            "equip.jewelrytype",
            "equip.rank",
            "loot",
            "lootitem",
            "monster",
            "signin"));
        configsInCode.removeAll(configsInZip);
        return configsInCode;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("missed: " + load(java.nio.file.Paths.get("configdata.zip"), "GBK", false));
    }
}
