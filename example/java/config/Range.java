package config;

public class Range {
    private int min;
    private int max;

    private Range() {
    }

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static Range _create(configgen.genjava.ConfigInput input) {
        Range self = new Range();
        self.min = input.readInt();
        self.max = input.readInt();
        return self;
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

}
