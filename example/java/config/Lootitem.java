package config;

public class Lootitem {
    private int lootid;
    private int itemid;
    private int chance;
    private int countmin;
    private int countmax;

    private void assign(Lootitem other) {
        lootid = other.lootid;
        itemid = other.itemid;
        chance = other.chance;
        countmin = other.countmin;
        countmax = other.countmax;
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
    public int hashCode() {
        return lootid + itemid;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof Lootitem))
            return false;
        Lootitem o = (Lootitem) other;
        return lootid == o.lootid && itemid == o.itemid;
    }

    @Override
    public String toString() {
        return "(" + lootid + "," + itemid + "," + chance + "," + countmin + "," + countmax + ")";
    }

    Lootitem _parse(java.util.List<String> data) {
        lootid = config.CSV.parseInt(data.get(0));
        itemid = config.CSV.parseInt(data.get(1));
        chance = config.CSV.parseInt(data.get(2));
        countmin = config.CSV.parseInt(data.get(3));
        countmax = config.CSV.parseInt(data.get(4));
        return this;
    }

    private static class Key {
        private int lootid;
        private int itemid;

        Key(int lootid, int itemid) {
            this.lootid = lootid;
            this.itemid = itemid;
        }

        @Override
        public int hashCode() {
            return lootid + itemid;
        }

        @Override
        public boolean equals(Object other) {
            if (null == other || !(other instanceof Key))
                return false;
            Key o = (Key) other;
            return lootid == o.lootid && itemid == o.itemid;
        }
    }

    private static final java.util.Map<Key, Lootitem> All = new java.util.LinkedHashMap<>();

    public static Lootitem get(int lootid, int itemid) {
        return All.get(new Key(lootid, itemid));
    }

    public static java.util.Collection<Lootitem> all() {
        return All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1, 2, 3, 4);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Lootitem self = new Lootitem()._parse(data);
            All.put(new Key(self.lootid, self.itemid), self);
        }
    }

    static void reload(java.util.List<java.util.List<String>> dataList) {
        java.util.Map<Key, Lootitem> old = new java.util.LinkedHashMap<>(All);
        All.clear();
        initialize(dataList);
        All.forEach((k, v) -> {
            Lootitem ov = old.get(k);
            if (ov != null)
                ov.assign(v);
        });
    }

}
