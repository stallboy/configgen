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

    private void assign(Ability other) {
        id = other.id;
        name = other.name;
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

    Ability _parse(java.util.List<String> data) {
        id = config.CSV.parseInt(data.get(0));
        name = data.get(1);
        return this;
    }

    private static final java.util.Map<Integer, Ability> All = new java.util.LinkedHashMap<>();

    public static Ability get(int id) {
        return All.get(id);
    }

    public static java.util.Collection<Ability> all() {
        return All.values();
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

    static void reload(java.util.List<java.util.List<String>> dataList) {
        java.util.Map<Integer, Ability> old = new java.util.LinkedHashMap<>(All);
        All.clear();
        initialize(dataList);
        All.forEach((k, v) -> {
            Ability ov = old.get(k);
            if (ov != null)
                ov.assign(v);
        });
    }

}
