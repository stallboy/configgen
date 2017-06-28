package config;

public class LootEnum {
    public static LootEnum COMBO1 = new LootEnum("combo1", 2);
    public static LootEnum COMBO2 = new LootEnum("combo2", 3);
    public static LootEnum COMBO3 = new LootEnum("combo3", 4);

    private String name;
    private int value;

    LootEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public config.Loot ref() {
        return config.Loot.get(value);
    }

}
