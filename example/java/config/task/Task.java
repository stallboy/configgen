package config.task;

public class Task {
    private int taskid;
    private String name = "";
    private String desc = "";
    private int nexttask;
    private config.task.Completecondition completecondition;
    private int exp;

    private Task() {
    }

    public static Task _create(ConfigInput input) {
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

    public void _resolve() {
        completecondition._resolve();
    }

    public static Task get(int taskid) {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.task_task_All.get(taskid);
    }

    public static java.util.Collection<Task> all() {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.task_task_All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Task self = new Task()._parse(data);
            All.put(self.taskid, self);
        }
    }

    static void resolve() {
        all().forEach(Task::_resolve);
    }

}
