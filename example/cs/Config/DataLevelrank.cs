using System;
using System.Collections.Generic;
using System.IO;

namespace Config
{
    public partial class DataLevelrank
    {
        public int Level { get; private set; } // 等级
        public int Rank { get; private set; } // 品质
        public Config.Equip.DataRank RefRank { get; private set; }

        public DataLevelrank() {
        }

        public DataLevelrank(int level, int rank) {
            this.Level = level;
            this.Rank = rank;
        }

        public override int GetHashCode()
        {
            return Level.GetHashCode() + Rank.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataLevelrank;
            return o != null && Level.Equals(o.Level) && Rank.Equals(o.Rank);
        }

        public override string ToString()
        {
            return "(" + Level + "," + Rank + ")";
        }

        internal static DataLevelrank _create(Config.Stream os)
        {
            var self = new DataLevelrank();
            self.Level = os.ReadInt32();
            self.Rank = os.ReadInt32();
            return self;
        }

        internal void _resolve(Config.LoadErrors errors)
        {
            RefRank = Config.Equip.DataRank.Get(Rank);
            if (RefRank == null) errors.RefNull("LevelRank", ToString(), "Rank", Rank);
	    }

    }
}
