using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Equip
{
    public partial class DataJewelrysuit
    {
        public int SuitID { get; private set; } // 饰品套装ID
        public string Name { get; private set; } // 策划用名字
        public int Ability1 { get; private set; } // 套装属性类型1（装备套装中的两件时增加的属性）
        public int Ability1Value { get; private set; } // 套装属性1
        public int Ability2 { get; private set; } // 套装属性类型2（装备套装中的三件时增加的属性）
        public int Ability2Value { get; private set; } // 套装属性2
        public int Ability3 { get; private set; } // 套装属性类型3（装备套装中的四件时增加的属性）
        public int Ability3Value { get; private set; } // 套装属性3
        public List<int> SuitList { get; private set; } // 部件1

        public override int GetHashCode()
        {
            return SuitID.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataJewelrysuit;
            return o != null && SuitID.Equals(o.SuitID);
        }

        public override string ToString()
        {
            return "(" + SuitID + "," + Name + "," + Ability1 + "," + Ability1Value + "," + Ability2 + "," + Ability2Value + "," + Ability3 + "," + Ability3Value + "," + CSV.ToString(SuitList) + ")";
        }

        static Config.KeyedList<int, DataJewelrysuit> all = null;

        public static DataJewelrysuit Get(int suitID)
        {
            DataJewelrysuit v;
            return all.TryGetValue(suitID, out v) ? v : null;
        }

        public static List<DataJewelrysuit> All()
        {
            return all.OrderedValues;
        }

        public static List<DataJewelrysuit> Filter(Predicate<DataJewelrysuit> predicate)
        {
            var r = new List<DataJewelrysuit>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<int, DataJewelrysuit>();
            for (var c = os.ReadInt32(); c > 0; c--) {
                var self = _create(os);
                all.Add(self.SuitID, self);
            }
        }

        internal static DataJewelrysuit _create(Config.Stream os)
        {
            var self = new DataJewelrysuit();
            self.SuitID = os.ReadInt32();
            self.Name = os.ReadString();
            self.Ability1 = os.ReadInt32();
            self.Ability1Value = os.ReadInt32();
            self.Ability2 = os.ReadInt32();
            self.Ability2Value = os.ReadInt32();
            self.Ability3 = os.ReadInt32();
            self.Ability3Value = os.ReadInt32();
            self.SuitList = new List<int>();
            for (var c = os.ReadInt32(); c > 0; c--)
                self.SuitList.Add(os.ReadInt32());
            return self;
        }

    }
}
