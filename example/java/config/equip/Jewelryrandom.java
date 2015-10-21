package config.equip;

public class Jewelryrandom {
    private config.LevelRank lvlRank = new config.LevelRank();
    private config.Range attackRange = new config.Range();
    private java.util.List<config.Range> otherRange = new java.util.ArrayList<>();

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
    public int hashCode() {
        return lvlRank.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof Jewelryrandom))
            return false;
        Jewelryrandom o = (Jewelryrandom) other;
        return lvlRank.equals(o.lvlRank);
    }

    @Override
    public String toString() {
        return "(" + lvlRank + "," + attackRange + "," + otherRange + ")";
    }

    Jewelryrandom _parse(java.util.List<String> data) {
        lvlRank._parse(data.subList(0, 2));
        attackRange._parse(data.subList(2, 4));
        String a = data.get(4);
        if (!a.isEmpty())
            otherRange.add(new config.Range()._parse(data.subList(4, 6)));
        a = data.get(6);
        if (!a.isEmpty())
            otherRange.add(new config.Range()._parse(data.subList(6, 8)));
        a = data.get(8);
        if (!a.isEmpty())
            otherRange.add(new config.Range()._parse(data.subList(8, 10)));
        a = data.get(10);
        if (!a.isEmpty())
            otherRange.add(new config.Range()._parse(data.subList(10, 12)));
        return this;
    }

    void _resolve() {
        lvlRank._resolve();
    }

    private static final java.util.Map<config.LevelRank, Jewelryrandom> All = new java.util.LinkedHashMap<>();

    public static Jewelryrandom get(config.LevelRank lvlRank) {
        return All.get(lvlRank);
    }

    public static java.util.Collection<Jewelryrandom> all() {
        return All.values();
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
