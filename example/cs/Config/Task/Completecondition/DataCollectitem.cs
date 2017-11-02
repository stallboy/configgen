using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Task.Completecondition
{
    public partial class DataCollectitem : Config.Task.DataCompletecondition
    {
        public override Config.Task.DataCompleteconditiontype type() {
            return Config.Task.DataCompleteconditiontype.CollectItem;
        }

        public int Itemid { get; private set; }
        public int Count { get; private set; }

        public DataCollectitem() {
        }

        public DataCollectitem(int itemid, int count) {
            this.Itemid = itemid;
            this.Count = count;
        }

        public override int GetHashCode()
        {
            return Itemid.GetHashCode() + Count.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataCollectitem;
            return o != null && Itemid.Equals(o.Itemid) && Count.Equals(o.Count);
        }

        public override string ToString()
        {
            return "(" + Itemid + "," + Count + ")";
        }

        internal new static DataCollectitem _create(Config.Stream os)
        {
            var self = new DataCollectitem();
            self.Itemid = os.ReadInt32();
            self.Count = os.ReadInt32();
            return self;
        }

    }
}
