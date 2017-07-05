import config.ConfigCodeSchema;
import config.ConfigMgr;
import config.ConfigMgrLoader;
import config.Loot;
import configgen.genjava.ConfigInput;
import configgen.genjava.Schema;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Schema codeSchema = ConfigCodeSchema.getCodeSchema();
        try (ConfigInput input = new ConfigInput(new DataInputStream(new BufferedInputStream(new FileInputStream("config.data"))))) {
            Schema dataSchema = Schema.create(input);
            boolean compatible = codeSchema.compatible(dataSchema);
            System.out.println("compatible " + compatible);

            if (compatible){
                ConfigMgr mgr = ConfigMgrLoader.load(input);
                ConfigMgr.setMgr(mgr);

                for (Loot loot : Loot.all()) {
                    System.out.println(loot);
                }
            }

        }
    }

}
