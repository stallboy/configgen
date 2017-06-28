package config.task.completecondition;

public class KillMonster implements config.task.Completecondition {
    @Override
    public config.task.Completeconditiontype type() {
        return config.task.Completeconditiontype.KILLMONSTER;
    }

    private int monsterid;
    private config.Monster RefMonsterid;
    private int count;

    private KillMonster() {
    }

    public KillMonster(int monsterid, int count) {
        this.monsterid = monsterid;
        this.count = count;
    }

    public static KillMonster _create(configgen.genjava.ConfigInput input) {
        KillMonster self = new KillMonster();
        self.monsterid = input.readInt();
        self.count = input.readInt();
        return self;
    }

    public int getMonsterid() {
        return monsterid;
    }

    public config.Monster refMonsterid() {
        return RefMonsterid;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int hashCode() {
        return monsterid + count;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof KillMonster))
            return false;
        KillMonster o = (KillMonster) other;
        return monsterid == o.monsterid && count == o.count;
    }

    @Override
    public String toString() {
        return "(" + monsterid + "," + count + ")";
    }

    @Override
    public void _resolve(config.ConfigMgr mgr) {
        RefMonsterid = mgr.monster_All.get(monsterid);
        java.util.Objects.requireNonNull(RefMonsterid);
    }

}
