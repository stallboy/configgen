using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Task.Completecondition
{
    public partial class DataConditionand : Config.Task.DataCompletecondition
    {
        public override Config.Task.DataCompleteconditiontype type() {
            return Config.Task.DataCompleteconditiontype.ConditionAnd;
        }

        public Config.Task.DataCompletecondition Cond1 { get; private set; }
        public Config.Task.DataCompletecondition Cond2 { get; private set; }

        public DataConditionand() {
        }

        public DataConditionand(Config.Task.DataCompletecondition cond1, Config.Task.DataCompletecondition cond2) {
            this.Cond1 = cond1;
            this.Cond2 = cond2;
        }

        public override int GetHashCode()
        {
            return Cond1.GetHashCode() + Cond2.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataConditionand;
            return o != null && Cond1.Equals(o.Cond1) && Cond2.Equals(o.Cond2);
        }

        public override string ToString()
        {
            return "(" + Cond1 + "," + Cond2 + ")";
        }

        internal new static DataConditionand _create(Config.Stream os)
        {
            var self = new DataConditionand();
            self.Cond1 = Config.Task.DataCompletecondition._create(os);
            self.Cond2 = Config.Task.DataCompletecondition._create(os);
            return self;
        }

        internal override void _resolve(Config.LoadErrors errors)
        {
            Cond1._resolve(errors);
            Cond2._resolve(errors);
	    }

    }
}
