package config;

public class Range {
    private int min;
    private int max;

    public Range() {
    }

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * 最小
     */
    public int getMin() {
        return min;
    }

    /**
     * 最大
     */
    public int getMax() {
        return max;
    }

    @Override
    public int hashCode() {
        return min + max;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof Range))
            return false;
        Range o = (Range) other;
        return min == o.min && max == o.max;
    }

    @Override
    public String toString() {
        return "(" + min + "," + max + ")";
    }

    public Range _parse(java.util.List<String> data) {
        min = config.CSV.parseInt(data.get(0));
        max = config.CSV.parseInt(data.get(1));
        return this;
    }

}
