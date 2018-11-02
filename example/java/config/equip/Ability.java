package config.equip;

public enum Ability {
    ATTACK("attack", 1),
    DEFENCE("defence", 2),
    HP("hp", 3),
    CRITICAL("critical", 4),
    CRITICAL_RESIST("critical_resist", 5),
    BLOCK("block", 6),
    BREAK_ARMOR("break_armor", 7);

    private String name;
    private int value;

    Ability(String name, int value) {
        this.name = name;
        this.value = value;
    }

    private static java.util.Map<Integer, Ability> map = new java.util.HashMap<>();

    static {
        for(Ability e : Ability.values()) {
            map.put(e.value, e);
        }
    }

    public static Ability get(int value) {
        return map.get(value);
    }

    /**
     * 属性类型
     */
    public int getId() {
        return value;
    }

    /**
     * 程序用名字
     */
    public String getName() {
        return name;
    }

}
