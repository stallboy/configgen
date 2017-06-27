package config.task;

public interface Completecondition {
    config.task.Completeconditiontype type();

    default void _resolve() {
    }

    static Completecondition _create(ConfigInput input) {
        switch(input.readStr()) {
            case "KillMonster":
                return new config.task.completecondition.KillMonster(input);
            case "TalkNpc":
                return new config.task.completecondition.TalkNpc(input);
            case "CollectItem":
                return new config.task.completecondition.CollectItem(input);
        }
        throw new IllegalArgumentException();
    }
}
