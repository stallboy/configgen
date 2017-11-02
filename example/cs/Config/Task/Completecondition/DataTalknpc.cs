using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Task.Completecondition
{
    public partial class DataTalknpc : Config.Task.DataCompletecondition
    {
        public override Config.Task.DataCompleteconditiontype type() {
            return Config.Task.DataCompleteconditiontype.TalkNpc;
        }

        public int Npcid { get; private set; }

        public DataTalknpc() {
        }

        public DataTalknpc(int npcid) {
            this.Npcid = npcid;
        }

        public override int GetHashCode()
        {
            return Npcid.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataTalknpc;
            return o != null && Npcid.Equals(o.Npcid);
        }

        public override string ToString()
        {
            return "(" + Npcid + ")";
        }

        internal new static DataTalknpc _create(Config.Stream os)
        {
            var self = new DataTalknpc();
            self.Npcid = os.ReadInt32();
            return self;
        }

    }
}
