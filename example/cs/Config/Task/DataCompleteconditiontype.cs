using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Task
{
    public partial class DataCompleteconditiontype
    {
        public static DataCompleteconditiontype KillMonster { get; private set; }
        public static DataCompleteconditiontype TalkNpc { get; private set; }
        public static DataCompleteconditiontype CollectItem { get; private set; }
        public static DataCompleteconditiontype ConditionAnd { get; private set; }
        public static DataCompleteconditiontype Chat { get; private set; }

        public int Id { get; private set; } // 任务完成条件类型（id的范围为1-100）
        public string Name { get; private set; } // 程序用名字

        public override int GetHashCode()
        {
            return Id.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataCompleteconditiontype;
            return o != null && Id.Equals(o.Id);
        }

        public override string ToString()
        {
            return "(" + Id + "," + Name + ")";
        }

        static Config.KeyedList<int, DataCompleteconditiontype> all = null;

        public static DataCompleteconditiontype Get(int id)
        {
            DataCompleteconditiontype v;
            return all.TryGetValue(id, out v) ? v : null;
        }

        public static List<DataCompleteconditiontype> All()
        {
            return all.OrderedValues;
        }

        public static List<DataCompleteconditiontype> Filter(Predicate<DataCompleteconditiontype> predicate)
        {
            var r = new List<DataCompleteconditiontype>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<int, DataCompleteconditiontype>();
            for (var c = os.ReadInt32(); c > 0; c--) {
                var self = _create(os);
                all.Add(self.Id, self);
                if (self.Name.Trim().Length == 0)
                    continue;
                switch(self.Name.Trim())
                {
                    case "KillMonster":
                        if (KillMonster != null)
                            errors.EnumDup("task.completeconditiontype", self.ToString());
                        KillMonster = self;
                        break;
                    case "TalkNpc":
                        if (TalkNpc != null)
                            errors.EnumDup("task.completeconditiontype", self.ToString());
                        TalkNpc = self;
                        break;
                    case "CollectItem":
                        if (CollectItem != null)
                            errors.EnumDup("task.completeconditiontype", self.ToString());
                        CollectItem = self;
                        break;
                    case "ConditionAnd":
                        if (ConditionAnd != null)
                            errors.EnumDup("task.completeconditiontype", self.ToString());
                        ConditionAnd = self;
                        break;
                    case "Chat":
                        if (Chat != null)
                            errors.EnumDup("task.completeconditiontype", self.ToString());
                        Chat = self;
                        break;
                    default:
                        errors.EnumDataAdd("task.completeconditiontype", self.ToString());
                        break;
                }
            }
            if (KillMonster == null)
                errors.EnumNull("task.completeconditiontype", "KillMonster");
            if (TalkNpc == null)
                errors.EnumNull("task.completeconditiontype", "TalkNpc");
            if (CollectItem == null)
                errors.EnumNull("task.completeconditiontype", "CollectItem");
            if (ConditionAnd == null)
                errors.EnumNull("task.completeconditiontype", "ConditionAnd");
            if (Chat == null)
                errors.EnumNull("task.completeconditiontype", "Chat");
        }

        internal static DataCompleteconditiontype _create(Config.Stream os)
        {
            var self = new DataCompleteconditiontype();
            self.Id = os.ReadInt32();
            self.Name = os.ReadString();
            return self;
        }

    }
}
