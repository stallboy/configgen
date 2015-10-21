package config.equip;

public enum Jewelrytype {
    JADEPENDANT,
    BRACELET,
    MAGICSEAL,
    BOTTLE;

    private int typeID;
    private String typeName = "";

    /**
     * 饰品类型
     */
    public int getTypeID() {
        return typeID;
    }

    /**
     * 程序用名字
     */
    public String getTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return "(" + typeID + "," + typeName + ")";
    }

    Jewelrytype _parse(java.util.List<String> data) {
        typeID = config.CSV.parseInt(data.get(0));
        typeName = data.get(1);
        return this;
    }

    private static final java.util.Map<Integer, Jewelrytype> All = new java.util.LinkedHashMap<>();

    public static Jewelrytype get(int typeID) {
        return All.get(typeID);
    }

    public static java.util.Collection<Jewelrytype> all() {
        return All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Jewelrytype self = valueOf(row.get(1).trim().toUpperCase())._parse(data);
            All.put(self.typeID, self);
        }
        if (values().length != all().size()) 
            throw new RuntimeException("Enum Uncompleted: Jewelrytype");
    }

}
