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

    private void assign(Loot other) {
        lootid = other.lootid;
        ename = other.ename;
        name = other.name;
        chanceList.clear();
        chanceList.addAll(other.chanceList);
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

    @Override
    public int hashCode() {
        return lootid;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof Loot))
            return false;
        Loot o = (Loot) other;
        return lootid == o.lootid;
    }

    @Override
    public String toString() {
        return "(" + lootid + "," + ename + "," + name + "," + chanceList + ")";
    }

    Loot _parse(java.util.List<String> data) {
        lootid = config.CSV.parseInt(data.get(0));
        ename = data.get(1);
        name = data.get(2);
        String a = data.get(3);
        if (!a.isEmpty())
            chanceList.add(config.CSV.parseInt(a));
        a = data.get(4);
        if (!a.isEmpty())
            chanceList.add(config.CSV.parseInt(a));
        a = data.get(5);
        if (!a.isEmpty())
            chanceList.add(config.CSV.parseInt(a));
        a = data.get(6);
        if (!a.isEmpty())
            chanceList.add(config.CSV.parseInt(a));
        a = data.get(7);
        if (!a.isEmpty())
            chanceList.add(config.CSV.parseInt(a));
        a = data.get(8);
        if (!a.isEmpty())
            chanceList.add(config.CSV.parseInt(a));
        a = data.get(9);
        if (!a.isEmpty())
            chanceList.add(config.CSV.parseInt(a));
        return this;
    }

    private static final java.util.Map<Integer, Loot> All = new java.util.LinkedHashMap<>();

    public static Loot get(int lootid) {
        return All.get(lootid);
    }

    public static java.util.Collection<Loot> all() {
        return All.values();
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

    static void reload(java.util.List<java.util.List<String>> dataList) {
        java.util.Map<Integer, Loot> old = new java.util.LinkedHashMap<>(All);
        All.clear();
        initialize(dataList);
        All.forEach((k, v) -> {
            Loot ov = old.get(k);
            if (ov != null)
                ov.assign(v);
        });
    }

}
