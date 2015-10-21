using System;
using System.Collections.Generic;
using System.IO;

namespace Config
{
    public partial class DataPosition
    {
        public int X { get; private set; }
        public int Y { get; private set; }
        public int Z { get; private set; }

        public DataPosition() {
        }

        public DataPosition(int x, int y, int z) {
            this.X = x;
            this.Y = y;
            this.Z = z;
        }

        public override int GetHashCode()
        {
            return X.GetHashCode() + Y.GetHashCode() + Z.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataPosition;
            return o != null && X.Equals(o.X) && Y.Equals(o.Y) && Z.Equals(o.Z);
        }

        public override string ToString()
        {
            return "(" + X + "," + Y + "," + Z + ")";
        }

        internal static DataPosition _create(Config.Stream os)
        {
            var self = new DataPosition();
            self.X = os.ReadInt32();
            self.Y = os.ReadInt32();
            self.Z = os.ReadInt32();
            return self;
        }

    }
}
