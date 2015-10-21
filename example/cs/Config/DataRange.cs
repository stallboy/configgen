using System;
using System.Collections.Generic;
using System.IO;

namespace Config
{
    public partial class DataRange
    {
        public int Min { get; private set; } // 最小
        public int Max { get; private set; } // 最大

        public DataRange() {
        }

        public DataRange(int min, int max) {
            this.Min = min;
            this.Max = max;
        }

        public override int GetHashCode()
        {
            return Min.GetHashCode() + Max.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataRange;
            return o != null && Min.Equals(o.Min) && Max.Equals(o.Max);
        }

        public override string ToString()
        {
            return "(" + Min + "," + Max + ")";
        }

        internal static DataRange _create(Config.Stream os)
        {
            var self = new DataRange();
            self.Min = os.ReadInt32();
            self.Max = os.ReadInt32();
            return self;
        }

    }
}
