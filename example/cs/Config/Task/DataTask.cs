using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Task
{
    public partial class DataTask
    {
        public int Taskid { get; private set; } // 任务完成条件类型（id的范围为1-100）
        public string Name { get; private set; } // 程序用名字
        public string Desc { get; private set; } // 注释
        public int Nexttask { get; private set; }
        public Config.Task.DataCompletecondition Completecondition { get; private set; }
        public int Exp { get; private set; }

        public override int GetHashCode()
        {
            return Taskid.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataTask;
            return o != null && Taskid.Equals(o.Taskid);
        }

        public override string ToString()
        {
            return "(" + Taskid + "," + Name + "," + Desc + "," + Nexttask + "," + Completecondition + "," + Exp + ")";
        }

        static Config.KeyedList<int, DataTask> all = null;

        public static DataTask Get(int taskid)
        {
            DataTask v;
            return all.TryGetValue(taskid, out v) ? v : null;
        }

        public static List<DataTask> All()
        {
            return all.OrderedValues;
        }

        public static List<DataTask> Filter(Predicate<DataTask> predicate)
        {
            var r = new List<DataTask>();
            foreach (var e in all.OrderedValues)
            {
                if (predicate(e))
                    r.Add(e);
            }
            return r;
        }

        internal static void Initialize(Config.Stream os, Config.LoadErrors errors)
        {
            all = new Config.KeyedList<int, DataTask>();
            for (var c = os.ReadSize(); c > 0; c--) {
                var self = _create(os);
                all.Add(self.Taskid, self);
            }
        }

        internal static void Resolve(Config.LoadErrors errors) {
            foreach (var v in All())
                v._resolve(errors);
        }

        internal static DataTask _create(Config.Stream os)
        {
            var self = new DataTask();
            self.Taskid = os.ReadInt32();
            self.Name = os.ReadString();
            self.Desc = os.ReadString();
            self.Nexttask = os.ReadInt32();
            self.Completecondition = Config.Task.DataCompletecondition._create(os);
            self.Exp = os.ReadInt32();
            return self;
        }

        internal void _resolve(Config.LoadErrors errors)
        {
            Completecondition._resolve(errors);
	    }

    }
}
