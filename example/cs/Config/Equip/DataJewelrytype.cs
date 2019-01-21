using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Equip
{
    public partial class DataJewelrytype
    {
        public static DataJewelrytype Jade { get; private set; }
        public static DataJewelrytype Bracelet { get; private set; }
        public static DataJewelrytype Magic { get; private set; }
        public static DataJewelrytype Bottle { get; private set; }

        public string TypeName { get; private set; } // ≥Ã–Ú”√√˚◊÷

        public override int GetHashCode()
        {
            return TypeName.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataJewelrytype;
            return o != null && TypeName.Equals(o.TypeName);
        }

        public override string ToString()
        {
            return "(" + TypeName + ")";
        }

        static Config.KeyedList<string, DataJewelrytype> all = null;

        public static DataJewelrytype Get(string typeName)
        {
            DataJewelrytype v;
            return all.TryGetValue(typeName, out v) ? v : null;
        }

        public static List<DataJewelrytype> All()
        {
            return all.OrderedValues;
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
            all = new Config.KeyedList<string, DataJewelrytype>();
            for (var c = os.ReadInt32(); c > 0; c--) {
                var self = _create(os);
                all.Add(self.TypeName, self);
                if (self.TypeName.Trim().Length == 0)
                    continue;
                switch(self.TypeName.Trim())
                {
                    case "Jade":
                        if (Jade != null)
                            errors.EnumDup("equip.jewelrytype", self.ToString());
                        Jade = self;
                        break;
                    case "Bracelet":
                        if (Bracelet != null)
                            errors.EnumDup("equip.jewelrytype", self.ToString());
                        Bracelet = self;
                        break;
                    case "Magic":
                        if (Magic != null)
                            errors.EnumDup("equip.jewelrytype", self.ToString());
                        Magic = self;
                        break;
                    case "Bottle":
                        if (Bottle != null)
                            errors.EnumDup("equip.jewelrytype", self.ToString());
                        Bottle = self;
                        break;
                    default:
                        errors.EnumDataAdd("equip.jewelrytype", self.ToString());
                        break;
                }
            }
            if (Jade == null)
                errors.EnumNull("equip.jewelrytype", "Jade");
            if (Bracelet == null)
                errors.EnumNull("equip.jewelrytype", "Bracelet");
            if (Magic == null)
                errors.EnumNull("equip.jewelrytype", "Magic");
            if (Bottle == null)
                errors.EnumNull("equip.jewelrytype", "Bottle");
        }

        internal static DataJewelrytype _create(Config.Stream os)
        {
            var self = new DataJewelrytype();
            self.TypeName = os.ReadString();
            return self;
        }

    }
}
