using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Equip
{
    public partial class DataJewelrytype
    {
        public static DataJewelrytype JADEPENDANT { get; private set; }
        public static DataJewelrytype BRACELET { get; private set; }
        public static DataJewelrytype MAGICSEAL { get; private set; }
        public static DataJewelrytype BOTTLE { get; private set; }

        public int TypeID { get; private set; } // 饰品类型
        public string TypeName { get; private set; } // 程序用名字

        public override int GetHashCode()
        {
            return TypeID.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataJewelrytype;
            return o != null && TypeID.Equals(o.TypeID);
        }

        public override string ToString()
        {
            return "(" + TypeID + "," + TypeName + ")";
        }

        static Config.KeyedList<int, DataJewelrytype> all = null;

        public static List<DataJewelrytype> All()
        {
            return all.OrderedValues;
        }

        public static DataJewelrytype Get(int typeID)
        {
            DataJewelrytype v;
            return all.TryGetValue(typeID, out v) ? v : null;
        }

        public static List<DataJewelrytype> Filter(Predicate<DataJewelrytype> predicate)
        {
            var r = new List<DataJewelrytype>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<int, DataJewelrytype>();
            for (var i = 0; i < os.ReadSize(); i++) {
                var self = _create(os);
                all.Add(self.TypeID, self);
                if (self.TypeName.Trim().Length == 0)
                    continue;
                switch(self.TypeName.Trim())
                {
                    case "JADEPENDANT":
                        if (JADEPENDANT != null)
                            errors.EnumDup("equip.jewelrytype", self.ToString());
                        JADEPENDANT = self;
                        break;
                    case "BRACELET":
                        if (BRACELET != null)
                            errors.EnumDup("equip.jewelrytype", self.ToString());
                        BRACELET = self;
                        break;
                    case "MAGICSEAL":
                        if (MAGICSEAL != null)
                            errors.EnumDup("equip.jewelrytype", self.ToString());
                        MAGICSEAL = self;
                        break;
                    case "BOTTLE":
                        if (BOTTLE != null)
                            errors.EnumDup("equip.jewelrytype", self.ToString());
                        BOTTLE = self;
                        break;
                    default:
                        errors.EnumDataAdd("equip.jewelrytype", self.ToString());
                        break;
                }
            }
            if (JADEPENDANT == null)
                errors.EnumNull("equip.jewelrytype", "JADEPENDANT");
            if (BRACELET == null)
                errors.EnumNull("equip.jewelrytype", "BRACELET");
            if (MAGICSEAL == null)
                errors.EnumNull("equip.jewelrytype", "MAGICSEAL");
            if (BOTTLE == null)
                errors.EnumNull("equip.jewelrytype", "BOTTLE");
        }

        internal static DataJewelrytype _create(Config.Stream os)
        {
            var self = new DataJewelrytype();
            self.TypeID = os.ReadInt32();
            self.TypeName = os.ReadString();
            return self;
        }

    }
}
