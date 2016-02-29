package config.task.completecondition;

public class TalkNpc implements config.task.Completecondition {
    @Override
    public config.task.Completeconditiontype type() {
        return config.task.Completeconditiontype.TALKNPC;
    }

    private int npcid;

    public TalkNpc() {
    }

    public TalkNpc(int npcid) {
        this.npcid = npcid;
    }

    public void assign(TalkNpc other) {
        npcid = other.npcid;
    }

    public int getNpcid() {
        return npcid;
    }

    @Override
    public int hashCode() {
        return npcid;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof TalkNpc))
            return false;
        TalkNpc o = (TalkNpc) other;
        return npcid == o.npcid;
    }

    @Override
    public String toString() {
        return "(" + npcid + ")";
    }

    public TalkNpc _parse(java.util.List<String> data) {
        npcid = config.CSV.parseInt(data.get(0));
        return this;
    }

}
