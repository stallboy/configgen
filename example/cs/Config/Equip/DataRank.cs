using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Equip
{
    public partial class DataRank
    {
        public static DataRank White { get; private set; }
        public static DataRank Green { get; private set; }
        public static DataRank Blue { get; private set; }
        public static DataRank Purple { get; private set; }
        public static DataRank Yellow { get; private set; }

        public int RankID { get; private set; } // 稀有度
        public string RankName { get; private set; } // 程序用名字
        public string RankShowName { get; private set; } // 显示名称

        public override int GetHashCode()
        {
            return RankID.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataRank;
            return o != null && RankID.Equals(o.RankID);
        }

        public override string ToString()
        {
            return "(" + RankID + "," + RankName + "," + RankShowName + ")";
        }

        static Config.KeyedList<int, DataRank> all = null;

        public static DataRank Get(int rankID)
        {
            DataRank v;
            return all.TryGetValue(rankID, out v) ? v : null;
        }

        public static List<DataRank> All()
        {
            return all.OrderedValues;
        }

        public static List<DataRank> Filter(Predicate<DataRank> predicate)
        {
            var r = new List<DataRank>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<int, DataRank>();
            for (var c = os.ReadSize(); c > 0; c--) {
                var self = _create(os);
                all.Add(self.RankID, self);
                if (self.RankName.Trim().Length == 0)
                    continue;
                switch(self.RankName.Trim())
                {
                    case "white":
                        if (White != null)
                            errors.EnumDup("equip.rank", self.ToString());
                        White = self;
                        break;
                    case "green":
                        if (Green != null)
                            errors.EnumDup("equip.rank", self.ToString());
                        Green = self;
                        break;
                    case "blue":
                        if (Blue != null)
                            errors.EnumDup("equip.rank", self.ToString());
                        Blue = self;
                        break;
                    case "purple":
                        if (Purple != null)
                            errors.EnumDup("equip.rank", self.ToString());
                        Purple = self;
                        break;
                    case "yellow":
                        if (Yellow != null)
                            errors.EnumDup("equip.rank", self.ToString());
                        Yellow = self;
                        break;
                    default:
                        errors.EnumDataAdd("equip.rank", self.ToString());
                        break;
                }
            }
            if (White == null)
                errors.EnumNull("equip.rank", "white");
            if (Green == null)
                errors.EnumNull("equip.rank", "green");
            if (Blue == null)
                errors.EnumNull("equip.rank", "blue");
            if (Purple == null)
                errors.EnumNull("equip.rank", "purple");
            if (Yellow == null)
                errors.EnumNull("equip.rank", "yellow");
        }

        internal static DataRank _create(Config.Stream os)
        {
            var self = new DataRank();
            self.RankID = os.ReadInt32();
            self.RankName = os.ReadString();
            self.RankShowName = os.ReadString();
            return self;
        }

    }
}
