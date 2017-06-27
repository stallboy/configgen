package config.equip;

public class Jewelry {
    private int iD;
    private String name = "";
    private String iconFile = "";
    private config.LevelRank lvlRank = new config.LevelRank();
    private String type = "";
    private config.equip.Jewelrytype RefType;
    private int suitID;
    private config.equip.Jewelrysuit NullableRefSuitID;
    private int keyAbility;
    private config.equip.Ability RefKeyAbility;
    private int keyAbilityValue;
    private int salePrice;
    private String description = "";

    private Jewelry() {
    }

    public static Jewelry _create(ConfigInput input) {
        Jewelry self = new Jewelry();
        self.iD = input.readInt();
        self.name = input.readStr();
        self.iconFile = input.readStr();
        self.lvlRank = config.LevelRank._create(input);
        self.type = input.readStr();
        self.suitID = input.readInt();
        self.keyAbility = input.readInt();
        self.keyAbilityValue = input.readInt();
        self.salePrice = input.readInt();
        self.description = input.readStr();
        return self;
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
    public String getType() {
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
    public String toString() {
        return "(" + iD + "," + name + "," + iconFile + "," + lvlRank + "," + type + "," + suitID + "," + keyAbility + "," + keyAbilityValue + "," + salePrice + "," + description + ")";
    }

    public void _resolve() {
        lvlRank._resolve();
        RefType = config.equip.Jewelrytype.getByTypeName(type);
        java.util.Objects.requireNonNull(RefType);
        NullableRefSuitID = config.equip.Jewelrysuit.get(suitID);
        RefKeyAbility = config.equip.Ability.get(keyAbility);
        java.util.Objects.requireNonNull(RefKeyAbility);
    }

    public static Jewelry get(int iD) {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.equip_jewelry_All.get(iD);
    }

    public static java.util.Collection<Jewelry> all() {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.equip_jewelry_All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Jewelry self = new Jewelry()._parse(data);
            All.put(self.iD, self);
        }
    }

    static void resolve() {
        all().forEach(Jewelry::_resolve);
    }

}
