package config;

public class Position {
    private int x;
    private int y;
    private int z;

    public Position() {
    }

    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void assign(Position other) {
        x = other.x;
        y = other.y;
        z = other.z;
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
        if (null == other || !(other instanceof Position))
            return false;
        Position o = (Position) other;
        return x == o.x && y == o.y && z == o.z;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

    public Position _parse(java.util.List<String> data) {
        data = config.CSV.parseList(data.get(0), ';');
        x = config.CSV.parseInt(data.get(0));
        y = config.CSV.parseInt(data.get(1));
        z = config.CSV.parseInt(data.get(2));
        return this;
    }

}
