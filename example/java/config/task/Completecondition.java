package config.task;

public interface Completecondition {
    config.task.Completeconditiontype type();

    default void _resolve() {
    }

    static Completecondition _parse(java.util.List<String> data) {
        switch(data.get(0)) {
            case "KillMonster":
                return new config.task.completecondition.KillMonster()._parse(data.subList(1, data.size()));
            case "TalkNpc":
                return new config.task.completecondition.TalkNpc()._parse(data.subList(1, data.size()));
            case "CollectItem":
                return new config.task.completecondition.CollectItem()._parse(data.subList(1, data.size()));
        }
        return null;
    }
}
