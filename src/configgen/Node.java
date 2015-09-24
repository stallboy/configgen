package configgen;

import java.util.LinkedList;
import java.util.List;

public class Node {
    public final Node root;
    protected final Node parent;
    protected String link = "";

    public Node(Node parent, String link) {
        this.parent = parent;
        this.link = link;
        root = parent == null ? this : parent.root;
    }

    public String location() {
        List<String> par = new LinkedList<>();
        for (Node p = this; p != null; p = p.parent) {
            par.add(0, p.link);
        }
        return String.join(".", par);
    }

    public void Assert(boolean cond, String... str) {
        if (!cond)
            throw new AssertionError(location() + ":" + String.join(",", str));
    }
}
