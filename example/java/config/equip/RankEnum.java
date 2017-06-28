package config.equip;

public enum RankEnum {
    WHITE("white", 1),
    GREEN("green", 2),
    BLUE("blue", 3),
    PURPLE("purple", 4),
    YELLOW("yellow", 5);

    private String name;
    private int value;

    RankEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    private static java.util.Map<Integer, RankEnum> map = new java.util.HashMap<>();

    static {
        for(RankEnum e : RankEnum.values()) {
            map.put(e.getValue(), e);
        }
    }

    public static RankEnum get(int value) {
        return map.get(value);
    }

    public config.equip.Rank ref() {
        return config.equip.Rank.get(value);
    }

}
