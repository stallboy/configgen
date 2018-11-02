package config;

public class Lootitem {
    private int lootid;
    private int itemid;
    private int chance;
    private int countmin;
    private int countmax;

    private Lootitem() {
    }

    public static Lootitem _create(configgen.genjava.ConfigInput input) {
        Lootitem self = new Lootitem();
        self.lootid = input.readInt();
        self.itemid = input.readInt();
        self.chance = input.readInt();
        self.countmin = input.readInt();
        self.countmax = input.readInt();
        return self;
    }

    /**
     * 掉落id
     */
    public int getLootid() {
        return lootid;
    }

    /**
     * 掉落物品
     */
    public int getItemid() {
        return itemid;
    }

    /**
     * 掉落概率
     */
    public int getChance() {
        return chance;
    }

    /**
     * 数量下限
     */
    public int getCountmin() {
        return countmin;
    }

    /**
     * 数量上限
     */
    public int getCountmax() {
        return countmax;
    }

    @Override
    public String toString() {
        return "(" + lootid + "," + itemid + "," + chance + "," + countmin + "," + countmax + ")";
    }

    public static class LootidItemidKey {
        private int lootid;
        private int itemid;

        LootidItemidKey(int lootid, int itemid) {
            this.lootid = lootid;
            this.itemid = itemid;
        }

        @Override
        public int hashCode() {
            return lootid + itemid;
        }

        @Override
        public boolean equals(Object other) {
            if (null == other || !(other instanceof LootidItemidKey))
                return false;
            LootidItemidKey o = (LootidItemidKey) other;
            return lootid == o.lootid && itemid == o.itemid;
        }
    }

    public static Lootitem get(int lootid, int itemid) {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.lootitem_All.get(new LootidItemidKey(lootid, itemid));
    }

    public static java.util.Collection<Lootitem> all() {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.lootitem_All.values();
    }

    public static void _createAll(config.ConfigMgr mgr, configgen.genjava.ConfigInput input) {
        for (int c = input.readInt(); c > 0; c--) {
            Lootitem self = Lootitem._create(input);
            mgr.lootitem_All.put(new LootidItemidKey(self.lootid, self.itemid), self);
        }
    }

}
