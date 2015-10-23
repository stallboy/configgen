using System;
using System.Collections.Generic;
using System.IO;

namespace Config
{
    public partial class DataSignin
    {
        public int Id { get; private set; } // 礼包ID
        public KeyedList<int, int> Item2countMap { get; private set; } // 普通奖励,
        public KeyedList<int, int> Vipitem2vipcountMap { get; private set; } // vip奖励,
        public int Viplevel { get; private set; } // 领取vip奖励的最低等级
        public string IconFile { get; private set; } // 礼包图标

        public override int GetHashCode()
        {
            return Id.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataSignin;
            return o != null && Id.Equals(o.Id);
        }

        public override string ToString()
        {
            return "(" + Id + "," + Item2countMap + "," + Vipitem2vipcountMap + "," + Viplevel + "," + IconFile + ")";
        }

        static Config.KeyedList<int, DataSignin> all = null;

        public static List<DataSignin> All()
        {
            return all.OrderedValues;
        }

        public static DataSignin Get(int id)
        {
            DataSignin v;
            return all.TryGetValue(id, out v) ? v : null;
        }

        public static List<DataSignin> Filter(Predicate<DataSignin> predicate)
        {
            var r = new List<DataSignin>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<int, DataSignin>();
            for (var i = 0; i < os.ReadSize(); i++) {
                var self = _create(os);
                all.Add(self.Id, self);
            }
        }

        internal static DataSignin _create(Config.Stream os)
        {
            var self = new DataSignin();
            self.Id = os.ReadInt32();
            self.Item2countMap = new KeyedList<int, int>();
            for (var c = (int)os.ReadSize(); c > 0; c--)
                self.Item2countMap.Add(os.ReadInt32(), os.ReadInt32());
            self.Vipitem2vipcountMap = new KeyedList<int, int>();
            for (var c = (int)os.ReadSize(); c > 0; c--)
                self.Vipitem2vipcountMap.Add(os.ReadInt32(), os.ReadInt32());
            self.Viplevel = os.ReadInt32();
            self.IconFile = os.ReadString();
            return self;
        }

    }
}
