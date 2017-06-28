package config.task;

public enum Completeconditiontype {
    KILLMONSTER("KillMonster", 1),
    TALKNPC("TalkNpc", 2),
    COLLECTITEM("CollectItem", 3);

    private String name;
    private int value;

    Completeconditiontype(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    private static java.util.Map<Integer, Completeconditiontype> map = new java.util.HashMap<>();

    static {
        for(Completeconditiontype e : Completeconditiontype.values()) {
            map.put(e.getValue(), e);
        }
    }

    public static Completeconditiontype get(int value) {
        return map.get(value);
    }

}
