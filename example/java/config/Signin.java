package config;

public class Signin {
    private int id;
    private java.util.Map<Integer, Integer> item2countMap = new java.util.LinkedHashMap<>();
    private java.util.Map<Integer, Integer> vipitem2vipcountMap = new java.util.LinkedHashMap<>();
    private int viplevel;
    private String iconFile;

    private Signin() {
    }

    public static Signin _create(configgen.genjava.ConfigInput input) {
        Signin self = new Signin();
        self.id = input.readInt();
        for (int c = input.readInt(); c > 0; c--) {
            self.item2countMap.put(input.readInt(), input.readInt());
        }
        for (int c = input.readInt(); c > 0; c--) {
            self.vipitem2vipcountMap.put(input.readInt(), input.readInt());
        }
        self.viplevel = input.readInt();
        self.iconFile = input.readStr();
        return self;
    }

    /**
     * 礼包ID
     */
    public int getId() {
        return id;
    }

    /**
     * 普通奖励
     */
    public java.util.Map<Integer, Integer> getItem2countMap() {
        return item2countMap;
    }

    /**
     * vip奖励
     */
    public java.util.Map<Integer, Integer> getVipitem2vipcountMap() {
        return vipitem2vipcountMap;
    }

    /**
     * 领取vip奖励的最低等级
     */
    public int getViplevel() {
        return viplevel;
    }

    /**
     * 礼包图标
     */
    public String getIconFile() {
        return iconFile;
    }

    @Override
    public String toString() {
        return "(" + id + "," + item2countMap + "," + vipitem2vipcountMap + "," + viplevel + "," + iconFile + ")";
    }

    public static Signin get(int id) {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.signin_All.get(id);
    }

    public static java.util.Collection<Signin> all() {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.signin_All.values();
    }

    public static void _createAll(config.ConfigMgr mgr, configgen.genjava.ConfigInput input) {
        for (int c = input.readInt(); c > 0; c--) {
            Signin self = Signin._create(input);
            mgr.signin_All.put(self.id, self);
        }
    }

}
