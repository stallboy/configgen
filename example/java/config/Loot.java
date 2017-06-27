package config;

public class Loot {
    private static Loot COMBO1_;
    private static Loot COMBO2_;
    private static Loot COMBO3_;

    public static Loot COMBO1() { return COMBO1_; }
    public static Loot COMBO2() { return COMBO2_; }
    public static Loot COMBO3() { return COMBO3_; }

    private int lootid;
    private String ename = "";
    private String name = "";
    private java.util.List<Integer> chanceList = new java.util.ArrayList<>();
    private java.util.List<config.Lootitem> ListRefLootid = new java.util.ArrayList<>();

    private Loot() {
    }

    public static Loot _create(ConfigInput input) {
        Loot self = new Loot();
        self.lootid = input.readInt();
        self.ename = input.readStr();
        self.name = input.readStr();
        for (int c = input.readInt(); c > 0; c--) {
            self.chanceList.add(input.readInt());
        }
        return self;
    }

    /**
     * 序号
     */
    public int getLootid() {
        return lootid;
    }

    public String getEname() {
        return ename;
    }

    /**
     * 名字
     */
    public String getName() {
        return name;
    }

    /**
     * 掉落0件物品的概率
     */
    public java.util.List<Integer> getChanceList() {
        return chanceList;
    }

    public java.util.List<config.Lootitem> listRefLootid() {
        return ListRefLootid;
    }

    @Override
    public String toString() {
        return "(" + lootid + "," + ename + "," + name + "," + chanceList + ")";
    }

    public void _resolve() {
        config.Lootitem.all().forEach( v -> {
            if (v.getLootid() == lootid)
                ListRefLootid.add(v);
        });
    }

    public static Loot get(int lootid) {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.loot_All.get(lootid);
    }

    public static java.util.Collection<Loot> all() {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.loot_All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Loot self = new Loot()._parse(data);
            All.put(self.lootid, self);
            String name = row.get(1).trim().toUpperCase();
            switch (name) {
                case "COMBO1":
                    COMBO1_ = self;
                    break;
                case "COMBO2":
                    COMBO2_ = self;
                    break;
                case "COMBO3":
                    COMBO3_ = self;
                    break;
            }
        }
        java.util.Objects.requireNonNull(COMBO1_);
        java.util.Objects.requireNonNull(COMBO2_);
        java.util.Objects.requireNonNull(COMBO3_);
    }

    static void resolve() {
        all().forEach(Loot::_resolve);
    }

}
