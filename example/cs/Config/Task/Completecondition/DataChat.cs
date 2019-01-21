using System;
using System.Collections.Generic;
using System.IO;

namespace Config.Task.Completecondition
{
    public partial class DataChat : Config.Task.DataCompletecondition
    {
        public override Config.Task.DataCompleteconditiontype type() {
            return Config.Task.DataCompleteconditiontype.Chat;
        }

        public string Msg { get; private set; }

        public DataChat() {
        }

        public DataChat(string msg) {
            this.Msg = msg;
        }

        public override int GetHashCode()
        {
            return Msg.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj == null) return false;
            if (obj == this) return true;
            var o = obj as DataChat;
            return o != null && Msg.Equals(o.Msg);
        }

        public override string ToString()
        {
            return "(" + Msg + ")";
        }

        internal new static DataChat _create(Config.Stream os)
        {
            var self = new DataChat();
            self.Msg = os.ReadString();
            return self;
        }

    }
}
