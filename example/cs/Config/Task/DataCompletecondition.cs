using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Task
{
    public partial class DataCompletecondition
    {

        public DataCompletecondition() {
        }

        public DataCompletecondition() {
        }

        public override int GetHashCode()
        {
            return ;
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataCompletecondition;
            return o != null && ;
        }

        public override string ToString()
        {
            return "(" +  + ")";
        }

        internal static DataCompletecondition _create(Config.Stream os)
        {
            var self = new DataCompletecondition();
            return self;
        }

        internal void _resolve(Config.LoadErrors errors)
        {
	    }

    }
}
