package config.equip;

public enum Ability {
    ATTACK,
    DEFENCE,
    HP,
    CRITICAL,
    CRITICAL_RESIST,
    BLOCK,
    BREAK_ARMOR;

    private int id;
    private String name = "";

    private Ability() {
    }

    public static Ability _create(ConfigInput input) {
        Ability self = new Ability();
        self.id = input.readInt();
        self.name = input.readStr();
        return self;
    }

    /**
     * 属性类型
     */
    public int getId() {
        return id;
    }

    /**
     * 程序用名字
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "(" + id + "," + name + ")";
    }

    public static Ability get(int id) {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.equip_ability_All.get(id);
    }

    public static java.util.Collection<Ability> all() {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.equip_ability_All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Ability self = valueOf(row.get(1).trim().toUpperCase())._parse(data);
            All.put(self.id, self);
        }
        if (values().length != all().size()) 
            throw new RuntimeException("Enum Uncompleted: Ability");
    }

}
