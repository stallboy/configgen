package config.equip;

public class Jewelryrandom {
    private config.LevelRank lvlRank = new config.LevelRank();
    private config.Range attackRange = new config.Range();
    private java.util.List<config.Range> otherRange = new java.util.ArrayList<>();

    private Jewelryrandom() {
    }

    public static Jewelryrandom _create(ConfigInput input) {
        Jewelryrandom self = new Jewelryrandom();
        self.lvlRank = config.LevelRank._create(input);
        self.attackRange = config.Range._create(input);
        for (int c = input.readInt(); c > 0; c--) {
            self.otherRange.add(config.Range._create(input));
        }
        return self;
    }

    /**
     * 等级
     */
    public config.LevelRank getLvlRank() {
        return lvlRank;
    }

    /**
     * 最小攻击力
     */
    public config.Range getAttackRange() {
        return attackRange;
    }

    /**
     * 最小防御力
     */
    public java.util.List<config.Range> getOtherRange() {
        return otherRange;
    }

    @Override
    public String toString() {
        return "(" + lvlRank + "," + attackRange + "," + otherRange + ")";
    }

    public void _resolve() {
        lvlRank._resolve();
    }

    public static Jewelryrandom get(config.LevelRank lvlRank) {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.equip_jewelryrandom_All.get(lvlRank);
    }

    public static java.util.Collection<Jewelryrandom> all() {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.equip_jewelryrandom_All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Jewelryrandom self = new Jewelryrandom()._parse(data);
            All.put(self.lvlRank, self);
        }
    }

    static void resolve() {
        all().forEach(Jewelryrandom::_resolve);
    }

}
