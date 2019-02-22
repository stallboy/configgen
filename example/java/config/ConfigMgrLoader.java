package config;

public class ConfigMgrLoader {

    public static ConfigMgr load(configgen.genjava.ConfigInput input) {
        ConfigMgr mgr = new ConfigMgr();
        int c = input.readInt();
        if (c < 10) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < c; i++) {
            String tableName = input.readStr();
            int tableSize = input.readInt();
            switch (tableName) {
                case "equip.jewelry":
                    config.equip.Jewelry._createAll(mgr, input);
                    break;
                case "equip.jewelryrandom":
                    config.equip.Jewelryrandom._createAll(mgr, input);
                    break;
                case "equip.jewelrysuit":
                    config.equip.Jewelrysuit._createAll(mgr, input);
                    break;
                case "equip.rank":
                    config.equip.Rank_Detail._createAll(mgr, input);
                    break;
                case "loot":
                    config.Loot._createAll(mgr, input);
                    break;
                case "lootitem":
                    config.Lootitem._createAll(mgr, input);
                    break;
                case "monster":
                    config.Monster._createAll(mgr, input);
                    break;
                case "signin":
                    config.Signin._createAll(mgr, input);
                    break;
                case "task.task":
                    config.task.Task._createAll(mgr, input);
                    break;
                case "task.taskextraexp":
                    config.task.Taskextraexp._createAll(mgr, input);
                    break;
                default:
                    input.skipBytes(tableSize);
                    break;
            }
        }

        config.equip.Jewelry._resolveAll(mgr);
        config.equip.Jewelryrandom._resolveAll(mgr);
        config.Loot._resolveAll(mgr);
        config.task.Task._resolveAll(mgr);
        return mgr;
    }
}
