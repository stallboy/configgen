package config.task.completecondition;

public class Chat implements config.task.Completecondition {
    @Override
    public config.task.Completeconditiontype type() {
        return config.task.Completeconditiontype.CHAT;
    }

    private String msg;

    private Chat() {
    }

    public Chat(String msg) {
        this.msg = msg;
    }

    public static Chat _create(configgen.genjava.ConfigInput input) {
        Chat self = new Chat();
        self.msg = input.readStr();
        return self;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public int hashCode() {
        return msg.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof Chat))
            return false;
        Chat o = (Chat) other;
        return msg.equals(o.msg);
    }

    @Override
    public String toString() {
        return "(" + msg + ")";
    }

}
