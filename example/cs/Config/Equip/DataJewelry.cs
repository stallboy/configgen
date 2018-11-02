using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Equip
{
    public partial class DataJewelry
    {
        public int ID { get; private set; } // 首饰ID
        public string Name { get; private set; } // 首饰名称
        public string IconFile { get; private set; } // 图标ID
        public Config.DataLevelrank LvlRank { get; private set; } // 首饰等级
        public string Type { get; private set; } // 首饰类型
        public Config.Equip.DataJewelrytype RefType { get; private set; }
        public int SuitID { get; private set; } // 套装ID（为0是没有不属于套装，首饰品级为4的首饰该参数为套装id，其余情况为0,引用JewelrySuit.csv）
        public Config.Equip.DataJewelrysuit NullableRefSuitID { get; private set; }
        public int KeyAbility { get; private set; } // 关键属性类型
        public Config.Equip.DataAbility RefKeyAbility { get; private set; }
        public int KeyAbilityValue { get; private set; } // 关键属性数值
        public int SalePrice { get; private set; } // 售卖价格
        public string Description { get; private set; } // 描述,根据Lvl和Rank来随机3个属性，第一个属性由Lvl,Rank行随机，剩下2个由Lvl和小于Rank的行里随机。Rank最小的时候都从Lvl，Rank里随机。

        public override int GetHashCode()
        {
            return ID.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataJewelry;
            return o != null && ID.Equals(o.ID);
        }

        public override string ToString()
        {
            return "(" + ID + "," + Name + "," + IconFile + "," + LvlRank + "," + Type + "," + SuitID + "," + KeyAbility + "," + KeyAbilityValue + "," + SalePrice + "," + Description + ")";
        }

        static Config.KeyedList<int, DataJewelry> all = null;

        public static DataJewelry Get(int iD)
        {
            DataJewelry v;
            return all.TryGetValue(iD, out v) ? v : null;
        }

        public static List<DataJewelry> All()
        {
            return all.OrderedValues;
        }

        public static List<DataJewelry> Filter(Predicate<DataJewelry> predicate)
        {
            var r = new List<DataJewelry>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<int, DataJewelry>();
            for (var c = os.ReadSize(); c > 0; c--) {
                var self = _create(os);
                all.Add(self.ID, self);
            }
        }

        internal static void Resolve(Config.LoadErrors errors) {
            foreach (var v in All())
                v._resolve(errors);
        }

        internal static DataJewelry _create(Config.Stream os)
        {
            var self = new DataJewelry();
            self.ID = os.ReadInt32();
            self.Name = os.ReadString();
            self.IconFile = os.ReadString();
            self.LvlRank = Config.DataLevelrank._create(os);
            self.Type = os.ReadString();
            self.SuitID = os.ReadInt32();
            self.KeyAbility = os.ReadInt32();
            self.KeyAbilityValue = os.ReadInt32();
            self.SalePrice = os.ReadInt32();
            self.Description = os.ReadString();
            return self;
        }

        internal void _resolve(Config.LoadErrors errors)
        {
            LvlRank._resolve(errors);
            RefType = Config.Equip.DataJewelrytype.Get(Type);
            if (RefType == null) errors.RefNull("equip.jewelry", ToString(), "Type", Type);
            NullableRefSuitID = Config.Equip.DataJewelrysuit.Get(SuitID);
            RefKeyAbility = Config.Equip.DataAbility.Get(KeyAbility);
            if (RefKeyAbility == null) errors.RefNull("equip.jewelry", ToString(), "KeyAbility", KeyAbility);
	    }

    }
}
