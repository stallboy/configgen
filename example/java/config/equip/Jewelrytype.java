package config.equip;

public enum Jewelrytype {
    JADE("Jade"),
    BRACELET("Bracelet"),
    MAGIC("Magic"),
    BOTTLE("Bottle");

    private String value;

    Jewelrytype(String value) {
        this.value = value;
    }

    private static java.util.Map<String, Jewelrytype> map = new java.util.HashMap<>();

    static {
        for(Jewelrytype e : Jewelrytype.values()) {
            map.put(e.value, e);
        }
    }

    public static Jewelrytype get(String value) {
        return map.get(value);
    }

    /**
     * 程序用名字
     */
    public String getTypeName() {
        return value;
    }

}
