package configgen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Node {
    public final Node root;
    protected final Node parent;
    protected String link = "";
    protected final List<Node> children = new ArrayList<>();

    public Node(Node parent, String link) {
        this.parent = parent;
        this.link = link;
        if (parent != null) {
            root = parent.root;
            parent.children.add(this);
        } else {
            root = this;
        }
    }

    public final String location() {
        List<String> par = new LinkedList<>();
        for (Node p = this; p != null; p = p.parent) {
            par.add(0, p.link);
        }
        return String.join(".", par);
    }

    public final void dump(PrintStream ps) {
        if (parent == null)
            for (Node child : children)
                child.dump(ps, "");
        else
            dump(ps, "");
    }

    private void dump(PrintStream ps, String tab) {
        ps.println(tab + "[" + getClass().getSimpleName() + "]" + link);
        for (Node child : children)
            child.dump(ps, tab + "\t");
    }

    public final void Assert(boolean cond, String... str) {
        if (!cond)
            throw new AssertionError(location() + ":" + String.join(",", str));
    }
}
