package config.equip;

public enum Rank {
    WHITE,
    GREEN,
    BLUE,
    PURPLE,
    YELLOW;

    private int rankID;
    private String rankName = "";
    private String rankShowName = "";

    /**
     * 稀有度
     */
    public int getRankID() {
        return rankID;
    }

    /**
     * 程序用名字
     */
    public String getRankName() {
        return rankName;
    }

    /**
     * 显示名称
     */
    public String getRankShowName() {
        return rankShowName;
    }

    @Override
    public String toString() {
        return "(" + rankID + "," + rankName + "," + rankShowName + ")";
    }

    Rank _parse(java.util.List<String> data) {
        rankID = config.CSV.parseInt(data.get(0));
        rankName = data.get(1);
        rankShowName = data.get(2);
        return this;
    }

    private static final java.util.Map<Integer, Rank> All = new java.util.LinkedHashMap<>();

    public static Rank get(int rankID) {
        return All.get(rankID);
    }

    public static java.util.Collection<Rank> all() {
        return All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1, 3);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Rank self = valueOf(row.get(1).trim().toUpperCase())._parse(data);
            All.put(self.rankID, self);
        }
        if (values().length != all().size()) 
            throw new RuntimeException("Enum Uncompleted: Rank");
    }

}
