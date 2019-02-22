package config.task;

public class Taskextraexp {
    private int taskid;
    private int extraexp;

    private Taskextraexp() {
    }

    public static Taskextraexp _create(configgen.genjava.ConfigInput input) {
        Taskextraexp self = new Taskextraexp();
        self.taskid = input.readInt();
        self.extraexp = input.readInt();
        return self;
    }

    /**
     * 任务完成条件类型（id的范围为1-100）
     */
    public int getTaskid() {
        return taskid;
    }

    /**
     * 额外奖励经验
     */
    public int getExtraexp() {
        return extraexp;
    }

    @Override
    public String toString() {
        return "(" + taskid + "," + extraexp + ")";
    }

    public static Taskextraexp get(int taskid) {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.task_taskextraexp_All.get(taskid);
    }

    public static java.util.Collection<Taskextraexp> all() {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.task_taskextraexp_All.values();
    }

    public static void _createAll(config.ConfigMgr mgr, configgen.genjava.ConfigInput input) {
        for (int c = input.readInt(); c > 0; c--) {
            Taskextraexp self = Taskextraexp._create(input);
            mgr.task_taskextraexp_All.put(self.taskid, self);
        }
    }

}
