package config;

public class Position {
    private int x;
    private int y;
    private int z;

    private Position() {
    }

    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Position _create(configgen.genjava.ConfigInput input) {
        Position self = new Position();
        self.x = input.readInt();
        self.y = input.readInt();
        self.z = input.readInt();
        return self;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return x + y + z;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Position))
            return false;
        Position o = (Position) other;
        return x == o.x && y == o.y && z == o.z;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

}
