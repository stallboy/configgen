package config.task;

public class Task {
    private int taskid;
    private String name = "";
    private String desc = "";
    private int nexttask;
    private config.task.Completecondition completecondition;
    private int exp;

    private void assign(Task other) {
        taskid = other.taskid;
        name = other.name;
        desc = other.desc;
        nexttask = other.nexttask;
        completecondition = other.completecondition;
        exp = other.exp;
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
    public int hashCode() {
        return taskid;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof Task))
            return false;
        Task o = (Task) other;
        return taskid == o.taskid;
    }

    @Override
    public String toString() {
        return "(" + taskid + "," + name + "," + desc + "," + nexttask + "," + completecondition + "," + exp + ")";
    }

    Task _parse(java.util.List<String> data) {
        taskid = config.CSV.parseInt(data.get(0));
        name = data.get(1);
        desc = data.get(2);
        nexttask = config.CSV.parseInt(data.get(3));
        completecondition = config.task.Completecondition._parse(data.subList(4, 7));
        exp = config.CSV.parseInt(data.get(7));
        return this;
    }

    void _resolve() {
        completecondition._resolve();
    }

    private static final java.util.Map<Integer, Task> All = new java.util.LinkedHashMap<>();

    public static Task get(int taskid) {
        return All.get(taskid);
    }

    public static java.util.Collection<Task> all() {
        return All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Task self = new Task()._parse(data);
            All.put(self.taskid, self);
        }
    }

    static void reload(java.util.List<java.util.List<String>> dataList) {
        java.util.Map<Integer, Task> old = new java.util.LinkedHashMap<>(All);
        All.clear();
        initialize(dataList);
        All.forEach((k, v) -> {
            Task ov = old.get(k);
            if (ov != null)
                ov.assign(v);
        });
    }

    static void resolve() {
        all().forEach(Task::_resolve);
    }

}
