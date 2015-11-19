package config.equip;

public class Jewelry {
    private int iD;
    private String name = "";
    private String iconFile = "";
    private config.LevelRank lvlRank = new config.LevelRank();
    private int type;
    private config.equip.Jewelrytype RefType;
    private int suitID;
    private config.equip.Jewelrysuit NullableRefSuitID;
    private int keyAbility;
    private config.equip.Ability RefKeyAbility;
    private int keyAbilityValue;
    private int salePrice;
    private String description = "";

    private void assign(Jewelry other) {
        iD = other.iD;
        name = other.name;
        iconFile = other.iconFile;
        lvlRank.assign(other.lvlRank);
        type = other.type;
        suitID = other.suitID;
        keyAbility = other.keyAbility;
        keyAbilityValue = other.keyAbilityValue;
        salePrice = other.salePrice;
        description = other.description;
    }

    /**
     * 首饰ID
     */
    public int getID() {
        return iD;
    }

    /**
     * 首饰名称
     */
    public String getName() {
        return name;
    }

    /**
     * 图标ID
     */
    public String getIconFile() {
        return iconFile;
    }

    /**
     * 首饰等级
     */
    public config.LevelRank getLvlRank() {
        return lvlRank;
    }

    /**
     * 首饰类型
     */
    public int getType() {
        return type;
    }

    public config.equip.Jewelrytype refType() {
        return RefType;
    }

    /**
     * 套装ID（为0是没有不属于套装，首饰品级为4的首饰该参数为套装id，其余情况为0,引用JewelrySuit.csv）
     */
    public int getSuitID() {
        return suitID;
    }

    public config.equip.Jewelrysuit nullableRefSuitID() {
        return NullableRefSuitID;
    }

    /**
     * 关键属性类型
     */
    public int getKeyAbility() {
        return keyAbility;
    }

    public config.equip.Ability refKeyAbility() {
        return RefKeyAbility;
    }

    /**
     * 关键属性数值
     */
    public int getKeyAbilityValue() {
        return keyAbilityValue;
    }

    /**
     * 售卖价格
     */
    public int getSalePrice() {
        return salePrice;
    }

    /**
     * 描述,根据Lvl和Rank来随机3个属性，第一个属性由Lvl,Rank行随机，剩下2个由Lvl和小于Rank的行里随机。Rank最小的时候都从Lvl，Rank里随机。
     */
    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return iD;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof Jewelry))
            return false;
        Jewelry o = (Jewelry) other;
        return iD == o.iD;
    }

    @Override
    public String toString() {
        return "(" + iD + "," + name + "," + iconFile + "," + lvlRank + "," + type + "," + suitID + "," + keyAbility + "," + keyAbilityValue + "," + salePrice + "," + description + ")";
    }

    Jewelry _parse(java.util.List<String> data) {
        iD = config.CSV.parseInt(data.get(0));
        name = data.get(1);
        iconFile = data.get(2);
        lvlRank._parse(data.subList(3, 5));
        type = config.CSV.parseInt(data.get(5));
        suitID = config.CSV.parseInt(data.get(6));
        keyAbility = config.CSV.parseInt(data.get(7));
        keyAbilityValue = config.CSV.parseInt(data.get(8));
        salePrice = config.CSV.parseInt(data.get(9));
        description = data.get(10);
        return this;
    }

    void _resolve() {
        lvlRank._resolve();
        RefType = config.equip.Jewelrytype.get(type);
        java.util.Objects.requireNonNull(RefType);
        NullableRefSuitID = config.equip.Jewelrysuit.get(suitID);
        RefKeyAbility = config.equip.Ability.get(keyAbility);
        java.util.Objects.requireNonNull(RefKeyAbility);
    }

    private static final java.util.Map<Integer, Jewelry> All = new java.util.LinkedHashMap<>();

    public static Jewelry get(int iD) {
        return All.get(iD);
    }

    public static java.util.Collection<Jewelry> all() {
        return All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Jewelry self = new Jewelry()._parse(data);
            All.put(self.iD, self);
        }
    }

    static void reload(java.util.List<java.util.List<String>> dataList) {
        java.util.Map<Integer, Jewelry> old = new java.util.LinkedHashMap<>(All);
        All.clear();
        initialize(dataList);
        All.forEach((k, v) -> {
            Jewelry ov = old.get(k);
            if (ov != null)
                ov.assign(v);
        });
    }

    static void resolve() {
        all().forEach(Jewelry::_resolve);
    }

}
