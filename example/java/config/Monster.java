package config;

public class Monster {
    private int id;
    private config.Position pos = new config.Position();

    /**
     * id
     */
    public int getId() {
        return id;
    }

    public config.Position getPos() {
        return pos;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof Monster))
            return false;
        Monster o = (Monster) other;
        return id == o.id;
    }

    @Override
    public String toString() {
        return "(" + id + "," + pos + ")";
    }

    Monster _parse(java.util.List<String> data) {
        id = config.CSV.parseInt(data.get(0));
        pos._parse(data.subList(1, 2));
        return this;
    }

    private static final java.util.Map<Integer, Monster> All = new java.util.LinkedHashMap<>();

    public static Monster get(int id) {
        return All.get(id);
    }

    public static java.util.Collection<Monster> all() {
        return All.values();
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
