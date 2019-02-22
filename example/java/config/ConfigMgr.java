package config;

public class ConfigMgr {
    private static volatile ConfigMgr mgr;

    public static ConfigMgr getMgr(){
        return mgr;
    }

    public static void setMgr(ConfigMgr newMgr){
        mgr = newMgr;
    }

    public final java.util.Map<Integer, config.equip.Jewelry> equip_jewelry_All = new java.util.LinkedHashMap<>();

    public final java.util.Map<config.LevelRank, config.equip.Jewelryrandom> equip_jewelryrandom_All = new java.util.LinkedHashMap<>();

    public final java.util.Map<Integer, config.equip.Jewelrysuit> equip_jewelrysuit_All = new java.util.LinkedHashMap<>();

    public final java.util.Map<Integer, config.equip.Rank_Detail> equip_rank_All = new java.util.LinkedHashMap<>();

    public final java.util.Map<Integer, config.Loot> loot_All = new java.util.LinkedHashMap<>();

    public final java.util.Map<config.Lootitem.LootidItemidKey, config.Lootitem> lootitem_All = new java.util.LinkedHashMap<>();

    public final java.util.Map<Integer, config.Monster> monster_All = new java.util.LinkedHashMap<>();

    public final java.util.Map<Integer, config.Signin> signin_All = new java.util.LinkedHashMap<>();

    public final java.util.Map<Integer, config.task.Task> task_task_All = new java.util.LinkedHashMap<>();

    public final java.util.Map<Integer, config.task.Taskextraexp> task_taskextraexp_All = new java.util.LinkedHashMap<>();

}
