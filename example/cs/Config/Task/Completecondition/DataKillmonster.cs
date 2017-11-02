using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Task.Completecondition
{
    public partial class DataKillmonster : Config.Task.DataCompletecondition
    {
        public override Config.Task.DataCompleteconditiontype type() {
            return Config.Task.DataCompleteconditiontype.KillMonster;
        }

        public int Monsterid { get; private set; }
        public Config.DataMonster RefMonsterid { get; private set; }
        public int Count { get; private set; }

        public DataKillmonster() {
        }

        public DataKillmonster(int monsterid, int count) {
            this.Monsterid = monsterid;
            this.Count = count;
        }

        public override int GetHashCode()
        {
            return Monsterid.GetHashCode() + Count.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataKillmonster;
            return o != null && Monsterid.Equals(o.Monsterid) && Count.Equals(o.Count);
        }

        public override string ToString()
        {
            return "(" + Monsterid + "," + Count + ")";
        }

        internal new static DataKillmonster _create(Config.Stream os)
        {
            var self = new DataKillmonster();
            self.Monsterid = os.ReadInt32();
            self.Count = os.ReadInt32();
            return self;
        }

        internal override void _resolve(Config.LoadErrors errors)
        {
            RefMonsterid = Config.DataMonster.Get(Monsterid);
            if (RefMonsterid == null) errors.RefNull("KillMonster", ToString(), "monsterid", Monsterid);
	    }

    }
}
