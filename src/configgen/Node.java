package configgen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Node {
    protected final Node root;
    protected final Node parent;
    public final String name;
    protected final List<Node> children = new ArrayList<>();

    public Node(Node parent, String name) {
        this.parent = parent;
        this.name = name;
        if (parent != null) {
            root = parent.root;
            parent.children.add(this);
        } else {
            root = this;
        }
    }

    public String fullName() {
        return (parent != null ? parent.fullName() + "." : "") + name;
    }

    public void dump(PrintStream ps) {
        if (parent == null)
            for (Node child : children)
                child.dump(ps, "");
        else
            dump(ps, "");
    }

    private void dump(PrintStream ps, String tab) {
        ps.println(tab + "[" + getClass().getSimpleName() + "]" + name);
        for (Node child : children)
            child.dump(ps, tab + "\t");
    }

    public void require(boolean cond, String... str) {
        if (!cond)
            throw new AssertionError(fullName() + ": " + String.join(",", str));
    }
}
