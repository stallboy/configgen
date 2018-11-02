package config.equip;

public class Jewelryrandom {
    private config.LevelRank lvlRank;
    private config.Range attackRange;
    private java.util.List<config.Range> otherRange = new java.util.ArrayList<>();

    private Jewelryrandom() {
    }

    public static Jewelryrandom _create(configgen.genjava.ConfigInput input) {
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

    public void _resolve(config.ConfigMgr mgr) {
        lvlRank._resolve(mgr);
    }

    public static Jewelryrandom get(config.LevelRank lvlRank) {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.equip_jewelryrandom_All.get(lvlRank);
    }

    public static java.util.Collection<Jewelryrandom> all() {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.equip_jewelryrandom_All.values();
    }

    public static void _createAll(config.ConfigMgr mgr, configgen.genjava.ConfigInput input) {
        for (int c = input.readInt(); c > 0; c--) {
            Jewelryrandom self = Jewelryrandom._create(input);
            mgr.equip_jewelryrandom_All.put(self.lvlRank, self);
        }
    }

    public static void _resolveAll(config.ConfigMgr mgr) {
        for (Jewelryrandom e : mgr.equip_jewelryrandom_All.values()) {
            e._resolve(mgr);
        }
    }

}
