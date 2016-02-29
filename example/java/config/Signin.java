package config;

public class Signin {
    private int id;
    private java.util.Map<Integer, Integer> item2countMap = new java.util.LinkedHashMap<>();
    private java.util.Map<Integer, Integer> vipitem2vipcountMap = new java.util.LinkedHashMap<>();
    private int viplevel;
    private String iconFile = "";

    private void assign(Signin other) {
        id = other.id;
        item2countMap.clear();
        item2countMap.putAll(other.item2countMap);
        vipitem2vipcountMap.clear();
        vipitem2vipcountMap.putAll(other.vipitem2vipcountMap);
        viplevel = other.viplevel;
        iconFile = other.iconFile;
    }

    /**
     * 礼包ID
     */
    public int getId() {
        return id;
    }

    /**
     * 普通奖励
     */
    public java.util.Map<Integer, Integer> getItem2countMap() {
        return item2countMap;
    }

    /**
     * vip奖励
     */
    public java.util.Map<Integer, Integer> getVipitem2vipcountMap() {
        return vipitem2vipcountMap;
    }

    /**
     * 领取vip奖励的最低等级
     */
    public int getViplevel() {
        return viplevel;
    }

    /**
     * 礼包图标
     */
    public String getIconFile() {
        return iconFile;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof Signin))
            return false;
        Signin o = (Signin) other;
        return id == o.id;
    }

    @Override
    public String toString() {
        return "(" + id + "," + item2countMap + "," + vipitem2vipcountMap + "," + viplevel + "," + iconFile + ")";
    }

    Signin _parse(java.util.List<String> data) {
        id = config.CSV.parseInt(data.get(0));
        String a = data.get(1);
        if (!a.isEmpty())
            item2countMap.put(config.CSV.parseInt(a), config.CSV.parseInt(data.get(2)));
        a = data.get(3);
        if (!a.isEmpty())
            item2countMap.put(config.CSV.parseInt(a), config.CSV.parseInt(data.get(4)));
        a = data.get(5);
        if (!a.isEmpty())
            item2countMap.put(config.CSV.parseInt(a), config.CSV.parseInt(data.get(6)));
        a = data.get(7);
        if (!a.isEmpty())
            item2countMap.put(config.CSV.parseInt(a), config.CSV.parseInt(data.get(8)));
        a = data.get(9);
        if (!a.isEmpty())
            item2countMap.put(config.CSV.parseInt(a), config.CSV.parseInt(data.get(10)));
        a = data.get(11);
        if (!a.isEmpty())
            vipitem2vipcountMap.put(config.CSV.parseInt(a), config.CSV.parseInt(data.get(12)));
        a = data.get(13);
        if (!a.isEmpty())
            vipitem2vipcountMap.put(config.CSV.parseInt(a), config.CSV.parseInt(data.get(14)));
        viplevel = config.CSV.parseInt(data.get(15));
        iconFile = data.get(16);
        return this;
    }

    private static final java.util.Map<Integer, Signin> All = new java.util.LinkedHashMap<>();

    public static Signin get(int id) {
        return All.get(id);
    }

    public static java.util.Collection<Signin> all() {
        return All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Signin self = new Signin()._parse(data);
            All.put(self.id, self);
        }
    }

    static void reload(java.util.List<java.util.List<String>> dataList) {
        java.util.Map<Integer, Signin> old = new java.util.LinkedHashMap<>(All);
        All.clear();
        initialize(dataList);
        All.forEach((k, v) -> {
            Signin ov = old.get(k);
            if (ov != null)
                ov.assign(v);
        });
    }

}
