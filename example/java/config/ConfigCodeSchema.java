package config;

import configgen.genjava.*;

public class ConfigCodeSchema {

    public static Schema getCodeSchema() {
        SchemaInterface s2 = new SchemaInterface();
        {
            SchemaBean s3 = new SchemaBean(false);
            s3.addColumn("x", SchemaPrimitive.SInt);
            s3.addColumn("y", SchemaPrimitive.SInt);
            s3.addColumn("z", SchemaPrimitive.SInt);
            s2.addImp("Position", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(true);
            s3.addColumn("SuitID", SchemaPrimitive.SInt);
            s3.addColumn("Name", SchemaPrimitive.SStr);
            s3.addColumn("Ability1", SchemaPrimitive.SInt);
            s3.addColumn("Ability1Value", SchemaPrimitive.SInt);
            s3.addColumn("Ability2", SchemaPrimitive.SInt);
            s3.addColumn("Ability2Value", SchemaPrimitive.SInt);
            s3.addColumn("Ability3", SchemaPrimitive.SInt);
            s3.addColumn("Ability3Value", SchemaPrimitive.SInt);
            s3.addColumn("SuitList", new SchemaList(SchemaPrimitive.SInt));
            s2.addImp("equip.jewelrysuit", s3);
        }
        {
            SchemaEnum s3 = new SchemaEnum(false, false);
            s3.addValue("Jade");
            s3.addValue("Bracelet");
            s3.addValue("Magic");
            s3.addValue("Bottle");
            s2.addImp("equip.jewelrytype", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(true);
            s3.addColumn("RankID", SchemaPrimitive.SInt);
            s3.addColumn("RankName", SchemaPrimitive.SStr);
            s3.addColumn("RankShowName", SchemaPrimitive.SStr);
            s2.addImp("equip.rank", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(false);
            s3.addColumn("Level", SchemaPrimitive.SInt);
            s3.addColumn("Rank", SchemaPrimitive.SInt);
            s2.addImp("LevelRank", s3);
        }
        {
            SchemaEnum s3 = new SchemaEnum(true, true);
            s3.addValue("combo1", 2);
            s3.addValue("combo2", 3);
            s3.addValue("combo3", 4);
            s2.addImp("lootEnum", s3);
        }
        {
            SchemaInterface s3 = new SchemaInterface();
            {
                SchemaBean s4 = new SchemaBean(false);
                s4.addColumn("npcid", SchemaPrimitive.SInt);
                s3.addImp("TalkNpc", s4);
            }
            {
                SchemaBean s4 = new SchemaBean(false);
                s4.addColumn("itemid", SchemaPrimitive.SInt);
                s4.addColumn("count", SchemaPrimitive.SInt);
                s3.addImp("CollectItem", s4);
            }
            {
                SchemaBean s4 = new SchemaBean(false);
                s4.addColumn("monsterid", SchemaPrimitive.SInt);
                s4.addColumn("count", SchemaPrimitive.SInt);
                s3.addImp("KillMonster", s4);
            }
            s2.addImp("task.completecondition", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(true);
            s3.addColumn("lootid", SchemaPrimitive.SInt);
            s3.addColumn("itemid", SchemaPrimitive.SInt);
            s3.addColumn("chance", SchemaPrimitive.SInt);
            s3.addColumn("countmin", SchemaPrimitive.SInt);
            s3.addColumn("countmax", SchemaPrimitive.SInt);
            s2.addImp("lootitem", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(false);
            s3.addColumn("Min", SchemaPrimitive.SInt);
            s3.addColumn("Max", SchemaPrimitive.SInt);
            s2.addImp("Range", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(true);
            s3.addColumn("id", SchemaPrimitive.SInt);
            s3.addColumn("posList", new SchemaList(new SchemaRef("Position")));
            s2.addImp("monster", s3);
        }
        {
            SchemaEnum s3 = new SchemaEnum(false, true);
            s3.addValue("attack", 1);
            s3.addValue("defence", 2);
            s3.addValue("hp", 3);
            s3.addValue("critical", 4);
            s3.addValue("critical_resist", 5);
            s3.addValue("block", 6);
            s3.addValue("break_armor", 7);
            s2.addImp("equip.ability", s3);
        }
        {
            SchemaEnum s3 = new SchemaEnum(false, true);
            s3.addValue("white", 1);
            s3.addValue("green", 2);
            s3.addValue("blue", 3);
            s3.addValue("purple", 4);
            s3.addValue("yellow", 5);
            s2.addImp("equip.rankEnum", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(true);
            s3.addColumn("id", SchemaPrimitive.SInt);
            s3.addColumn("item2countMap", new SchemaMap(SchemaPrimitive.SInt, SchemaPrimitive.SInt));
            s3.addColumn("vipitem2vipcountMap", new SchemaMap(SchemaPrimitive.SInt, SchemaPrimitive.SInt));
            s3.addColumn("viplevel", SchemaPrimitive.SInt);
            s3.addColumn("IconFile", SchemaPrimitive.SStr);
            s2.addImp("signin", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(true);
            s3.addColumn("LvlRank", new SchemaRef("LevelRank"));
            s3.addColumn("AttackRange", new SchemaRef("Range"));
            s3.addColumn("OtherRange", new SchemaList(new SchemaRef("Range")));
            s2.addImp("equip.jewelryrandom", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(true);
            s3.addColumn("lootid", SchemaPrimitive.SInt);
            s3.addColumn("ename", SchemaPrimitive.SStr);
            s3.addColumn("name", SchemaPrimitive.SStr);
            s3.addColumn("chanceList", new SchemaList(SchemaPrimitive.SInt));
            s2.addImp("loot", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(true);
            s3.addColumn("taskid", SchemaPrimitive.SInt);
            s3.addColumn("name", SchemaPrimitive.SStr);
            s3.addColumn("desc", SchemaPrimitive.SStr);
            s3.addColumn("nexttask", SchemaPrimitive.SInt);
            s3.addColumn("completecondition", new SchemaRef("task.completecondition"));
            s3.addColumn("exp", SchemaPrimitive.SInt);
            s2.addImp("task.task", s3);
        }
        {
            SchemaBean s3 = new SchemaBean(true);
            s3.addColumn("ID", SchemaPrimitive.SInt);
            s3.addColumn("Name", SchemaPrimitive.SStr);
            s3.addColumn("IconFile", SchemaPrimitive.SStr);
            s3.addColumn("LvlRank", new SchemaRef("LevelRank"));
            s3.addColumn("Type", SchemaPrimitive.SStr);
            s3.addColumn("SuitID", SchemaPrimitive.SInt);
            s3.addColumn("KeyAbility", SchemaPrimitive.SInt);
            s3.addColumn("KeyAbilityValue", SchemaPrimitive.SInt);
            s3.addColumn("SalePrice", SchemaPrimitive.SInt);
            s3.addColumn("Description", SchemaPrimitive.SStr);
            s2.addImp("equip.jewelry", s3);
        }
        {
            SchemaEnum s3 = new SchemaEnum(false, true);
            s3.addValue("KillMonster", 1);
            s3.addValue("TalkNpc", 2);
            s3.addValue("CollectItem", 3);
            s2.addImp("task.completeconditiontype", s3);
        }
        return s2;
    }
}
