using System;
using System.Collections.Generic;
using System.IO;

namespace Config
{
    public partial class DataLootitem
    {
        public int Lootid { get; private set; } // 掉落id
        public int Itemid { get; private set; } // 掉落物品
        public int Chance { get; private set; } // 掉落概率
        public int Countmin { get; private set; } // 数量下限
        public int Countmax { get; private set; } // 数量上限

        public override int GetHashCode()
        {
            return Lootid.GetHashCode() + Itemid.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataLootitem;
            return o != null && Lootid.Equals(o.Lootid) && Itemid.Equals(o.Itemid);
        }

        public override string ToString()
        {
            return "(" + Lootid + "," + Itemid + "," + Chance + "," + Countmin + "," + Countmax + ")";
        }

        class LootidItemidKey
        {
            readonly int Lootid;
            readonly int Itemid;

            public LootidItemidKey(int lootid, int itemid)
            {
                this.Lootid = lootid;
                this.Itemid = itemid;
            }

            public override int GetHashCode()
            {
                return Lootid.GetHashCode() + Itemid.GetHashCode();
            }
            public override bool Equals(object obj)
            {
                if (obj == null) return false;
                if (obj == this) return true;
                var o = obj as LootidItemidKey;
                return o != null && Lootid.Equals(o.Lootid) && Itemid.Equals(o.Itemid);
            }
        }

        static Config.KeyedList<LootidItemidKey, DataLootitem> all = null;

        public static DataLootitem Get(int lootid, int itemid)
        {
            DataLootitem v;
            return all.TryGetValue(new LootidItemidKey(lootid, itemid), out v) ? v : null;
        }

        public static List<DataLootitem> All()
        {
            return all.OrderedValues;
        }

        public static List<DataLootitem> Filter(Predicate<DataLootitem> predicate)
        {
            var r = new List<DataLootitem>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<LootidItemidKey, DataLootitem>();
            for (var c = os.ReadSize(); c > 0; c--) {
                var self = _create(os);
                all.Add(new LootidItemidKey(self.Lootid, self.Itemid), self);
            }
        }

        internal static DataLootitem _create(Config.Stream os)
        {
            var self = new DataLootitem();
            self.Lootid = os.ReadInt32();
            self.Itemid = os.ReadInt32();
            self.Chance = os.ReadInt32();
            self.Countmin = os.ReadInt32();
            self.Countmax = os.ReadInt32();
            return self;
        }

    }
}
