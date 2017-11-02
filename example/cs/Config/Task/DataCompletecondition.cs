using System;
using System.Collections.Generic;
using System.IO;
namespace Config.Task
{
public abstract class DataCompletecondition
{
    public abstract Config.Task.DataCompleteconditiontype type();

    internal virtual void _resolve(Config.LoadErrors errors)
    {
    }

    internal static DataCompletecondition _create(Config.Stream os) {
        switch(os.ReadString()) {
            case "KillMonster":
                return Config.Task.Completecondition.DataKillmonster._create(os);
            case "TalkNpc":
                return Config.Task.Completecondition.DataTalknpc._create(os);
            case "CollectItem":
                return Config.Task.Completecondition.DataCollectitem._create(os);
        }
        return null;
    }
}
}
