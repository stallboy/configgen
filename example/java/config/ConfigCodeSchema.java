package config;

import configgen.genjava.*;

public class ConfigCodeSchema {

    public static Schema getCodeSchema() {
        SchemaInterface schema = new SchemaInterface();
        schema.addImp("LevelRank", LevelRank());
        schema.addImp("Position", Position());
        schema.addImp("Range", Range());
        schema.addImp("task.completecondition", task_completecondition());
        schema.addImp("equip.ability", equip_ability());
        schema.addImp("equip.jewelry", equip_jewelry());
        schema.addImp("equip.jewelryrandom", equip_jewelryrandom());
        schema.addImp("equip.jewelrysuit", equip_jewelrysuit());
        schema.addImp("equip.jewelrytype", equip_jewelrytype());
        schema.addImp("equip.rank", equip_rank());
        schema.addImp("equip.rank_Detail", equip_rank_Detail());
        schema.addImp("loot", loot());
        schema.addImp("lootitem", lootitem());
        schema.addImp("monster", monster());
        schema.addImp("signin", signin());
        schema.addImp("task.completeconditiontype", task_completeconditiontype());
        schema.addImp("task.task", task_task());
        schema.addImp("task.taskextraexp", task_taskextraexp());
        return schema;
    }

    private static Schema LevelRank() {
        SchemaBean s2 = new SchemaBean(false);
        s2.addColumn("Level", SchemaPrimitive.SInt);
        s2.addColumn("Rank", SchemaPrimitive.SInt);
        return s2;
    }

    private static Schema Position() {
        SchemaBean s2 = new SchemaBean(false);
        s2.addColumn("x", SchemaPrimitive.SInt);
        s2.addColumn("y", SchemaPrimitive.SInt);
        s2.addColumn("z", SchemaPrimitive.SInt);
        return s2;
    }

    private static Schema Range() {
        SchemaBean s2 = new SchemaBean(false);
        s2.addColumn("Min", SchemaPrimitive.SInt);
        s2.addColumn("Max", SchemaPrimitive.SInt);
        return s2;
    }

    private static Schema task_completecondition() {
        SchemaInterface s2 = new SchemaInterface();
        {
            SchemaBean s3 = new SchemaBean(false);
            s3.addColumn("monsterid", SchemaPrimitive.SInt);
            s3.addColumn("count", SchemaPrimitive.SInt);
            s2.addImp("KillMonster", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(false);
            s3.addColumn("npcid", SchemaPrimitive.SInt);
            s2.addImp("TalkNpc", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(false);
            s3.addColumn("msg", SchemaPrimitive.SStr);
            s2.addImp("Chat", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(false);
            s3.addColumn("cond1", new SchemaRef("task.completecondition"));
            s3.addColumn("cond2", new SchemaRef("task.completecondition"));
            s2.addImp("ConditionAnd", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(false);
            s3.addColumn("itemid", SchemaPrimitive.SInt);
            s3.addColumn("count", SchemaPrimitive.SInt);
            s2.addImp("CollectItem", s3);
        }
        return s2;
    }

    private static Schema equip_ability() {
        SchemaEnum s2 = new SchemaEnum(false, true);
        s2.addValue("attack", 1);
        s2.addValue("defence", 2);
        s2.addValue("hp", 3);
        s2.addValue("critical", 4);
        s2.addValue("critical_resist", 5);
        s2.addValue("block", 6);
        s2.addValue("break_armor", 7);
        return s2;
    }

    private static Schema equip_jewelry() {
        SchemaBean s2 = new SchemaBean(true);
        s2.addColumn("ID", SchemaPrimitive.SInt);
        s2.addColumn("Name", SchemaPrimitive.SStr);
        s2.addColumn("IconFile", SchemaPrimitive.SStr);
        s2.addColumn("LvlRank", new SchemaRef("LevelRank"));
        s2.addColumn("Type", SchemaPrimitive.SStr);
        s2.addColumn("SuitID", SchemaPrimitive.SInt);
        s2.addColumn("KeyAbility", SchemaPrimitive.SInt);
        s2.addColumn("KeyAbilityValue", SchemaPrimitive.SInt);
        s2.addColumn("SalePrice", SchemaPrimitive.SInt);
        s2.addColumn("Description", SchemaPrimitive.SStr);
        return s2;
    }

    private static Schema equip_jewelryrandom() {
        SchemaBean s2 = new SchemaBean(true);
        s2.addColumn("LvlRank", new SchemaRef("LevelRank"));
        s2.addColumn("AttackRange", new SchemaRef("Range"));
        s2.addColumn("OtherRange", new SchemaList(new SchemaRef("Range")));
        s2.addColumn("TestRange", new SchemaList(new SchemaRef("Range")));
        return s2;
    }

    private static Schema equip_jewelrysuit() {
        SchemaBean s2 = new SchemaBean(true);
        s2.addColumn("SuitID", SchemaPrimitive.SInt);
        s2.addColumn("Name", SchemaPrimitive.SStr);
        s2.addColumn("Ability1", SchemaPrimitive.SInt);
        s2.addColumn("Ability1Value", SchemaPrimitive.SInt);
        s2.addColumn("Ability2", SchemaPrimitive.SInt);
        s2.addColumn("Ability2Value", SchemaPrimitive.SInt);
        s2.addColumn("Ability3", SchemaPrimitive.SInt);
        s2.addColumn("Ability3Value", SchemaPrimitive.SInt);
        s2.addColumn("SuitList", new SchemaList(SchemaPrimitive.SInt));
        return s2;
    }

    private static Schema equip_jewelrytype() {
        SchemaEnum s2 = new SchemaEnum(false, false);
        s2.addValue("Jade");
        s2.addValue("Bracelet");
        s2.addValue("Magic");
        s2.addValue("Bottle");
        return s2;
    }

    private static Schema equip_rank() {
        SchemaEnum s2 = new SchemaEnum(false, true);
        s2.addValue("white", 1);
        s2.addValue("green", 2);
        s2.addValue("blue", 3);
        s2.addValue("purple", 4);
        s2.addValue("yellow", 5);
        return s2;
    }

    private static Schema equip_rank_Detail() {
        SchemaBean s2 = new SchemaBean(true);
        s2.addColumn("RankID", SchemaPrimitive.SInt);
        s2.addColumn("RankName", SchemaPrimitive.SStr);
        s2.addColumn("RankShowName", SchemaPrimitive.SStr);
        return s2;
    }

    private static Schema loot() {
        SchemaBean s2 = new SchemaBean(true);
        s2.addColumn("lootid", SchemaPrimitive.SInt);
        s2.addColumn("ename", SchemaPrimitive.SStr);
        s2.addColumn("name", SchemaPrimitive.SStr);
        s2.addColumn("chanceList", new SchemaList(SchemaPrimitive.SInt));
        return s2;
    }

    private static Schema lootitem() {
        SchemaBean s2 = new SchemaBean(true);
        s2.addColumn("lootid", SchemaPrimitive.SInt);
        s2.addColumn("itemid", SchemaPrimitive.SInt);
        s2.addColumn("chance", SchemaPrimitive.SInt);
        s2.addColumn("countmin", SchemaPrimitive.SInt);
        s2.addColumn("countmax", SchemaPrimitive.SInt);
        return s2;
    }

    private static Schema monster() {
        SchemaBean s2 = new SchemaBean(true);
        s2.addColumn("id", SchemaPrimitive.SInt);
        s2.addColumn("posList", new SchemaList(new SchemaRef("Position")));
        return s2;
    }

    private static Schema signin() {
        SchemaBean s2 = new SchemaBean(true);
        s2.addColumn("id", SchemaPrimitive.SInt);
        s2.addColumn("item2countMap", new SchemaMap(SchemaPrimitive.SInt, SchemaPrimitive.SInt));
        s2.addColumn("vipitem2vipcountMap", new SchemaMap(SchemaPrimitive.SInt, SchemaPrimitive.SInt));
        s2.addColumn("viplevel", SchemaPrimitive.SInt);
        s2.addColumn("IconFile", SchemaPrimitive.SStr);
        return s2;
    }

    private static Schema task_completeconditiontype() {
        SchemaEnum s2 = new SchemaEnum(false, true);
        s2.addValue("KillMonster", 1);
        s2.addValue("TalkNpc", 2);
        s2.addValue("CollectItem", 3);
        s2.addValue("ConditionAnd", 4);
        s2.addValue("Chat", 5);
        return s2;
    }

    private static Schema task_task() {
        SchemaBean s2 = new SchemaBean(true);
        s2.addColumn("taskid", SchemaPrimitive.SInt);
        s2.addColumn("name", SchemaPrimitive.SStr);
        s2.addColumn("desc", SchemaPrimitive.SStr);
        s2.addColumn("nexttask", SchemaPrimitive.SInt);
        s2.addColumn("completecondition", new SchemaRef("task.completecondition"));
        s2.addColumn("exp", SchemaPrimitive.SInt);
        return s2;
    }

    private static Schema task_taskextraexp() {
        SchemaBean s2 = new SchemaBean(true);
        s2.addColumn("taskid", SchemaPrimitive.SInt);
        s2.addColumn("extraexp", SchemaPrimitive.SInt);
        return s2;
    }

}
