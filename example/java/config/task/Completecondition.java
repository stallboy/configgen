package config.task;

public interface Completecondition {
    config.task.Completeconditiontype type();

    default void _resolve(config.ConfigMgr mgr) {
    }

    static Completecondition _create(configgen.genjava.ConfigInput input) {
        switch(input.readStr()) {
            case "KillMonster":
                return config.task.completecondition.KillMonster._create(input);
            case "TalkNpc":
                return config.task.completecondition.TalkNpc._create(input);
            case "Chat":
                return config.task.completecondition.Chat._create(input);
            case "ConditionAnd":
                return config.task.completecondition.ConditionAnd._create(input);
            case "CollectItem":
                return config.task.completecondition.CollectItem._create(input);
        }
        throw new IllegalArgumentException();
    }
}
