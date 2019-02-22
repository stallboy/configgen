using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Task
{
    public partial class DataTaskextraexp
    {
        public int Taskid { get; private set; } // 任务完成条件类型（id的范围为1-100）
        public int Extraexp { get; private set; } // 额外奖励经验

        public override int GetHashCode()
        {
            return Taskid.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataTaskextraexp;
            return o != null && Taskid.Equals(o.Taskid);
        }

        public override string ToString()
        {
            return "(" + Taskid + "," + Extraexp + ")";
        }

        static Config.KeyedList<int, DataTaskextraexp> all = null;

        public static DataTaskextraexp Get(int taskid)
        {
            DataTaskextraexp v;
            return all.TryGetValue(taskid, out v) ? v : null;
        }

        public static List<DataTaskextraexp> All()
        {
            return all.OrderedValues;
        }

        public static List<DataTaskextraexp> Filter(Predicate<DataTaskextraexp> predicate)
        {
            var r = new List<DataTaskextraexp>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<int, DataTaskextraexp>();
            for (var c = os.ReadInt32(); c > 0; c--) {
                var self = _create(os);
                all.Add(self.Taskid, self);
            }
        }

        internal static DataTaskextraexp _create(Config.Stream os)
        {
            var self = new DataTaskextraexp();
            self.Taskid = os.ReadInt32();
            self.Extraexp = os.ReadInt32();
            return self;
        }

    }
}
