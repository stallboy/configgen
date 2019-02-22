package config.task;

public class Task {
    private int taskid;
    private config.task.Taskextraexp NullableRefTaskid;
    private String name;
    private String desc;
    private int nexttask;
    private config.task.Completecondition completecondition;
    private int exp;

    private Task() {
    }

    public static Task _create(configgen.genjava.ConfigInput input) {
        Task self = new Task();
        self.taskid = input.readInt();
        self.name = input.readStr();
        self.desc = input.readStr();
        self.nexttask = input.readInt();
        self.completecondition = config.task.Completecondition._create(input);
        self.exp = input.readInt();
        return self;
    }

    /**
     * 任务完成条件类型（id的范围为1-100）
     */
    public int getTaskid() {
        return taskid;
    }

    public config.task.Taskextraexp nullableRefTaskid() {
        return NullableRefTaskid;
    }

    /**
     * 程序用名字
     */
    public String getName() {
        return name;
    }

    /**
     * 注释
     */
    public String getDesc() {
        return desc;
    }

    public int getNexttask() {
        return nexttask;
    }

    public config.task.Completecondition getCompletecondition() {
        return completecondition;
    }

    public int getExp() {
        return exp;
    }

    @Override
    public String toString() {
        return "(" + taskid + "," + name + "," + desc + "," + nexttask + "," + completecondition + "," + exp + ")";
    }

    public void _resolve(config.ConfigMgr mgr) {
        NullableRefTaskid = mgr.task_taskextraexp_All.get(taskid);
        completecondition._resolve(mgr);
    }

    public static Task get(int taskid) {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.task_task_All.get(taskid);
    }

    public static java.util.Collection<Task> all() {
        config.ConfigMgr mgr = config.ConfigMgr.getMgr();
        return mgr.task_task_All.values();
    }

    public static void _createAll(config.ConfigMgr mgr, configgen.genjava.ConfigInput input) {
        for (int c = input.readInt(); c > 0; c--) {
            Task self = Task._create(input);
            mgr.task_task_All.put(self.taskid, self);
        }
    }

    public static void _resolveAll(config.ConfigMgr mgr) {
        for (Task e : mgr.task_task_All.values()) {
            e._resolve(mgr);
        }
    }

}
