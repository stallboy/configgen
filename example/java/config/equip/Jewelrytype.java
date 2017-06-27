package config.equip;

public enum Jewelrytype {
    JADE,
    BRACELET,
    MAGIC,
    BOTTLE;

    private int typeID;
    private String typeName = "";

    private Jewelrytype() {
    }

    public static Jewelrytype _create(ConfigInput input) {
        Jewelrytype self = new Jewelrytype();
        self.typeID = input.readInt();
        self.typeName = input.readStr();
        return self;
    }

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

    public static Jewelrytype get(int typeID) {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.equip_jewelrytype_All.get(typeID);
    }

    public static Jewelrytype getByTypeName(String typeName) {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.equip_jewelrytype_TypeNameMap.get(typeName);
    }

    public static java.util.Collection<Jewelrytype> all() {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.equip_jewelrytype_All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Jewelrytype self = valueOf(row.get(1).trim().toUpperCase())._parse(data);
            All.put(self.typeID, self);
            TypeNameMap.put(self.typeName, self);
        }
        if (values().length != all().size()) 
            throw new RuntimeException("Enum Uncompleted: Jewelrytype");
    }

}
