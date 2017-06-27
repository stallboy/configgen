package config;

public class Monster {
    private int id;
    private java.util.List<config.Position> posList = new java.util.ArrayList<>();

    private Monster() {
    }

    public static Monster _create(ConfigInput input) {
        Monster self = new Monster();
        self.id = input.readInt();
        for (int c = input.readInt(); c > 0; c--) {
            self.posList.add(config.Position._create(input));
        }
        return self;
    }

    /**
     * id
     */
    public int getId() {
        return id;
    }

    public java.util.List<config.Position> getPosList() {
        return posList;
    }

    @Override
    public String toString() {
        return "(" + id + "," + posList + ")";
    }

    public static Monster get(int id) {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.monster_All.get(id);
    }

    public static java.util.Collection<Monster> all() {
        ConfigMgr mgr = ConfigMgr.getMgr();
        return mgr.monster_All.values();
    }

    static void initialize(java.util.List<java.util.List<String>> dataList) {
        java.util.List<Integer> indexes = java.util.Arrays.asList(0, 1);
        for (java.util.List<String> row : dataList) {
            java.util.List<String> data = indexes.stream().map(row::get).collect(java.util.stream.Collectors.toList());
            Monster self = new Monster()._parse(data);
            All.put(self.id, self);
        }
    }

}
