using System.Collections.Generic;

namespace Config
{
    public static class CSVProcessor
    {
        public static readonly LoadErrors Errors = new LoadErrors();

        public static void Process(Config.Stream os)
        {
            var configNulls = new List<string>
            {
                "equip.ability",
                "equip.jewelry",
                "equip.jewelryrandom",
                "equip.jewelrysuit",
                "equip.jewelrytype",
                "equip.rank",
                "loot",
                "lootitem",
                "monster",
                "signin",
            };
            for(;;)
            {
                var csv = os.ReadCfg();
                if (csv == null)
                    break;
                switch(csv)
                {
                    case "equip.ability":
                        configNulls.Remove(csv);
                        Config.Equip.DataAbility.Initialize(os, Errors);
                        break;
                    case "equip.jewelry":
                        configNulls.Remove(csv);
                        Config.Equip.DataJewelry.Initialize(os, Errors);
                        break;
                    case "equip.jewelryrandom":
                        configNulls.Remove(csv);
                        Config.Equip.DataJewelryrandom.Initialize(os, Errors);
                        break;
                    case "equip.jewelrysuit":
                        configNulls.Remove(csv);
                        Config.Equip.DataJewelrysuit.Initialize(os, Errors);
                        break;
                    case "equip.jewelrytype":
                        configNulls.Remove(csv);
                        Config.Equip.DataJewelrytype.Initialize(os, Errors);
                        break;
                    case "equip.rank":
                        configNulls.Remove(csv);
                        Config.Equip.DataRank.Initialize(os, Errors);
                        break;
                    case "loot":
                        configNulls.Remove(csv);
                        Config.DataLoot.Initialize(os, Errors);
                        break;
                    case "lootitem":
                        configNulls.Remove(csv);
                        Config.DataLootitem.Initialize(os, Errors);
                        break;
                    case "monster":
                        configNulls.Remove(csv);
                        Config.DataMonster.Initialize(os, Errors);
                        break;
                    case "signin":
                        configNulls.Remove(csv);
                        Config.DataSignin.Initialize(os, Errors);
                        break;
                    default:
                        Errors.ConfigDataAdd(csv);
                        break;
                }
            }
            foreach (var csv in configNulls)
                Errors.ConfigNull(csv);
            Config.Equip.DataJewelry.Resolve(Errors);
            Config.Equip.DataJewelryrandom.Resolve(Errors);
            Config.DataLoot.Resolve(Errors);
        }

    }
}

