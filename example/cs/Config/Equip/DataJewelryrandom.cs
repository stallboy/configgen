using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Equip
{
    public partial class DataJewelryrandom
    {
        public Config.DataLevelrank LvlRank { get; private set; } // 等级
        public Config.DataRange AttackRange { get; private set; } // 最小攻击力
        public List<Config.DataRange> OtherRange { get; private set; } // 最小防御力

        public override int GetHashCode()
        {
            return LvlRank.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataJewelryrandom;
            return o != null && LvlRank.Equals(o.LvlRank);
        }

        public override string ToString()
        {
            return "(" + LvlRank + "," + AttackRange + "," + CSV.ToString(OtherRange) + ")";
        }

        static Config.KeyedList<Config.DataLevelrank, DataJewelryrandom> all = null;

        public static DataJewelryrandom Get(Config.DataLevelrank lvlRank)
        {
            DataJewelryrandom v;
            return all.TryGetValue(lvlRank, out v) ? v : null;
        }

        public static List<DataJewelryrandom> All()
        {
            return all.OrderedValues;
        }

        public static List<DataJewelryrandom> Filter(Predicate<DataJewelryrandom> predicate)
        {
            var r = new List<DataJewelryrandom>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<Config.DataLevelrank, DataJewelryrandom>();
            for (var c = os.ReadSize(); c > 0; c--) {
                var self = _create(os);
                all.Add(self.LvlRank, self);
            }
        }

        internal static void Resolve(Config.LoadErrors errors) {
            foreach (var v in All())
                v._resolve(errors);
        }

        internal static DataJewelryrandom _create(Config.Stream os)
        {
            var self = new DataJewelryrandom();
            self.LvlRank = Config.DataLevelrank._create(os);
            self.AttackRange = Config.DataRange._create(os);
            self.OtherRange = new List<Config.DataRange>();
            for (var c = (int)os.ReadSize(); c > 0; c--)
                self.OtherRange.Add(Config.DataRange._create(os));
            return self;
        }

        internal void _resolve(Config.LoadErrors errors)
        {
            LvlRank._resolve(errors);
	    }

    }
}
