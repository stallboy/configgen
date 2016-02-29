package config.task.completecondition;

public class KillMonster implements config.task.Completecondition {
    @Override
    public config.task.Completeconditiontype type() {
        return config.task.Completeconditiontype.KILLMONSTER;
    }

    private int monsterid;
    private config.Monster RefMonsterid;
    private int count;

    public KillMonster() {
    }

    public KillMonster(int monsterid, int count) {
        this.monsterid = monsterid;
        this.count = count;
    }

    public void assign(KillMonster other) {
        monsterid = other.monsterid;
        count = other.count;
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

    public KillMonster _parse(java.util.List<String> data) {
        monsterid = config.CSV.parseInt(data.get(0));
        count = config.CSV.parseInt(data.get(1));
        return this;
    }

    @Override
    public void _resolve() {
        RefMonsterid = config.Monster.get(monsterid);
        java.util.Objects.requireNonNull(RefMonsterid);
    }

}
