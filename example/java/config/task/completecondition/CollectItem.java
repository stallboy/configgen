package config.task.completecondition;

public class CollectItem implements config.task.Completecondition {
    @Override
    public config.task.Completeconditiontype type() {
        return config.task.Completeconditiontype.COLLECTITEM;
    }

    private int itemid;
    private int count;

    public CollectItem() {
    }

    public CollectItem(int itemid, int count) {
        this.itemid = itemid;
        this.count = count;
    }

    public void assign(CollectItem other) {
        itemid = other.itemid;
        count = other.count;
    }

    public int getItemid() {
        return itemid;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int hashCode() {
        return itemid + count;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof CollectItem))
            return false;
        CollectItem o = (CollectItem) other;
        return itemid == o.itemid && count == o.count;
    }

    @Override
    public String toString() {
        return "(" + itemid + "," + count + ")";
    }

    public CollectItem _parse(java.util.List<String> data) {
        itemid = config.CSV.parseInt(data.get(0));
        count = config.CSV.parseInt(data.get(1));
        return this;
    }

}
