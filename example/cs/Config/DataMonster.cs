using System;
using System.Collections.Generic;
using System.IO;

namespace Config
{
    public partial class DataMonster
    {
        public int Id { get; private set; } // id
        public Config.DataPosition Pos { get; private set; }

        public override int GetHashCode()
        {
            return Id.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataMonster;
            return o != null && Id.Equals(o.Id);
        }

        public override string ToString()
        {
            return "(" + Id + "," + Pos + ")";
        }

        static Config.KeyedList<int, DataMonster> all = null;

        public static List<DataMonster> All()
        {
            return all.OrderedValues;
        }

        public static DataMonster Get(int id)
        {
            DataMonster v;
            return all.TryGetValue(id, out v) ? v : null;
        }

        public static List<DataMonster> Filter(Predicate<DataMonster> predicate)
        {
            var r = new List<DataMonster>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<int, DataMonster>();
            for (var c = os.ReadSize(); c > 0; c--) {
                var self = _create(os);
                all.Add(self.Id, self);
            }
        }

        internal static DataMonster _create(Config.Stream os)
        {
            var self = new DataMonster();
            self.Id = os.ReadInt32();
            self.Pos = Config.DataPosition._create(os);
            return self;
        }

    }
}
