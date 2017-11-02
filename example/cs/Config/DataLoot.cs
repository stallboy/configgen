using System;
using System.Collections.Generic;
using System.IO;

namespace Config
{
    public partial class DataLoot
    {
        public int Lootid { get; private set; } // 序号
        public string Ename { get; private set; }
        public string Name { get; private set; } // 名字
        public List<int> ChanceList { get; private set; } // 掉落0件物品的概率
        public List<Config.DataLootitem> ListRefLootid { get; private set; }

        public override int GetHashCode()
        {
            return Lootid.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataLoot;
            return o != null && Lootid.Equals(o.Lootid);
        }

        public override string ToString()
        {
            return "(" + Lootid + "," + Ename + "," + Name + "," + CSV.ToString(ChanceList) + ")";
        }

        static Config.KeyedList<int, DataLoot> all = null;

        public static DataLoot Get(int lootid)
        {
            DataLoot v;
            return all.TryGetValue(lootid, out v) ? v : null;
        }

        public static List<DataLoot> All()
        {
            return all.OrderedValues;
        }

        public static List<DataLoot> Filter(Predicate<DataLoot> predicate)
        {
            var r = new List<DataLoot>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<int, DataLoot>();
            for (var c = os.ReadSize(); c > 0; c--) {
                var self = _create(os);
                all.Add(self.Lootid, self);
            }
        }

        internal static void Resolve(Config.LoadErrors errors) {
            foreach (var v in All())
                v._resolve(errors);
        }

        internal static DataLoot _create(Config.Stream os)
        {
            var self = new DataLoot();
            self.Lootid = os.ReadInt32();
            self.Ename = os.ReadString();
            self.Name = os.ReadString();
            self.ChanceList = new List<int>();
            for (var c = (int)os.ReadSize(); c > 0; c--)
                self.ChanceList.Add(os.ReadInt32());
            return self;
        }

        internal void _resolve(Config.LoadErrors errors)
        {
            ListRefLootid = new List<Config.DataLootitem>();
            foreach (var v in Config.DataLootitem.All())
            {
            if (v.Lootid.Equals(Lootid))
                ListRefLootid.Add(v);
            }
	    }

    }
}
