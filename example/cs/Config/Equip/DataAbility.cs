using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Equip
{
    public partial class DataAbility
    {
        public static DataAbility Attack { get; private set; }
        public static DataAbility Defence { get; private set; }
        public static DataAbility Hp { get; private set; }
        public static DataAbility Critical { get; private set; }
        public static DataAbility Critical_resist { get; private set; }
        public static DataAbility Block { get; private set; }
        public static DataAbility Break_armor { get; private set; }

        public int Id { get; private set; } // 属性类型
        public string Name { get; private set; } // 程序用名字

        public override int GetHashCode()
        {
            return Id.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataAbility;
            return o != null && Id.Equals(o.Id);
        }

        public override string ToString()
        {
            return "(" + Id + "," + Name + ")";
        }

        static Config.KeyedList<int, DataAbility> all = null;

        public static DataAbility Get(int id)
        {
            DataAbility v;
            return all.TryGetValue(id, out v) ? v : null;
        }

        public static List<DataAbility> All()
        {
            return all.OrderedValues;
        }

        public static List<DataAbility> Filter(Predicate<DataAbility> predicate)
        {
            var r = new List<DataAbility>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<int, DataAbility>();
            for (var c = os.ReadInt32(); c > 0; c--) {
                var self = _create(os);
                all.Add(self.Id, self);
                if (self.Name.Trim().Length == 0)
                    continue;
                switch(self.Name.Trim())
                {
                    case "attack":
                        if (Attack != null)
                            errors.EnumDup("equip.ability", self.ToString());
                        Attack = self;
                        break;
                    case "defence":
                        if (Defence != null)
                            errors.EnumDup("equip.ability", self.ToString());
                        Defence = self;
                        break;
                    case "hp":
                        if (Hp != null)
                            errors.EnumDup("equip.ability", self.ToString());
                        Hp = self;
                        break;
                    case "critical":
                        if (Critical != null)
                            errors.EnumDup("equip.ability", self.ToString());
                        Critical = self;
                        break;
                    case "critical_resist":
                        if (Critical_resist != null)
                            errors.EnumDup("equip.ability", self.ToString());
                        Critical_resist = self;
                        break;
                    case "block":
                        if (Block != null)
                            errors.EnumDup("equip.ability", self.ToString());
                        Block = self;
                        break;
                    case "break_armor":
                        if (Break_armor != null)
                            errors.EnumDup("equip.ability", self.ToString());
                        Break_armor = self;
                        break;
                    default:
                        errors.EnumDataAdd("equip.ability", self.ToString());
                        break;
                }
            }
            if (Attack == null)
                errors.EnumNull("equip.ability", "attack");
            if (Defence == null)
                errors.EnumNull("equip.ability", "defence");
            if (Hp == null)
                errors.EnumNull("equip.ability", "hp");
            if (Critical == null)
                errors.EnumNull("equip.ability", "critical");
            if (Critical_resist == null)
                errors.EnumNull("equip.ability", "critical_resist");
            if (Block == null)
                errors.EnumNull("equip.ability", "block");
            if (Break_armor == null)
                errors.EnumNull("equip.ability", "break_armor");
        }

        internal static DataAbility _create(Config.Stream os)
        {
            var self = new DataAbility();
            self.Id = os.ReadInt32();
            self.Name = os.ReadString();
            return self;
        }

    }
}
