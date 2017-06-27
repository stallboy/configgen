package config.task;

public enum Completeconditiontype {
    KILLMONSTER,
    TALKNPC,
    COLLECTITEM;

    private int id;
    private String name = "";

    private Completeconditiontype() {
    }

    public static Completeconditiontype _create(ConfigInput input) {
        Completeconditiontype self = new Completeconditiontype();
        self.id = input.readInt();
        self.name = input.readStr();
        return self;
    }

    /**
     * 任务完成条件类型（id的范围为1-100）
     */
    public int getId() {
        return id;
    }

    /**
     * 程序用名字
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "(" + id + "," + name + ")";
    }

    public static Completeconditiontype get(int id) {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.task_completeconditiontype_All.get(id);
    }

    public static java.util.Collection<Completeconditiontype> all() {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.task_completeconditiontype_All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Completeconditiontype self = valueOf(row.get(1).trim().toUpperCase())._parse(data);
            All.put(self.id, self);
        }
        if (values().length != all().size()) 
            throw new RuntimeException("Enum Uncompleted: Completeconditiontype");
    }

}
