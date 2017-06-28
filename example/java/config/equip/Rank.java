package config.equip;

public class Rank {
    private int rankID;
    private String rankName;
    private String rankShowName;

    private Rank() {
    }

    public static Rank _create(configgen.genjava.ConfigInput input) {
        Rank self = new Rank();
        self.rankID = input.readInt();
        self.rankName = input.readStr();
        self.rankShowName = input.readStr();
        return self;
    }

    /**
     * 稀有度
     */
    public int getRankID() {
        return rankID;
    }

    /**
     * 程序用名字
     */
    public String getRankName() {
        return rankName;
    }

    /**
     * 显示名称
     */
    public String getRankShowName() {
        return rankShowName;
    }

    @Override
    public String toString() {
        return "(" + rankID + "," + rankName + "," + rankShowName + ")";
    }

    public static Rank get(int rankID) {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.equip_rank_All.get(rankID);
    }

    public static java.util.Collection<Rank> all() {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.equip_rank_All.values();
    }

    public static void _createAll(config.ConfigMgr mgr, configgen.genjava.ConfigInput input) {
        for (int c = input.readInt(); c > 0; c--) {
            Rank self = Rank._create(input);
            mgr.equip_rank_All.put(self.rankID, self);
        }
    }

}
