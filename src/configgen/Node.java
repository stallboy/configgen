package configgen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Node {
    protected final Node root;
    public final Node parent;
    public final String name;

    private List<Node> children;

    public Node(Node parent, String name) {
        this.parent = parent;
        this.name = name;
        if (parent != null) {
            root = parent.root;
            if (parent.children == null) {
                parent.children = new ArrayList<>();
            }
            parent.children.add(this);
        } else {
            root = this;
        }
    }

    public String fullName() {
        return (parent != null ? parent.fullName() + "." : "") + name;
    }

    public void dump(PrintStream ps) {
        if (parent == null) {
            if (children != null) {
                for (Node child : children) {
                    child.dump(ps, "");
                }
            }
        } else {
            dump(ps, "");
        }
    }

    private void dump(PrintStream ps, String tab) {
        ps.println(tab + "[" + getClass().getSimpleName() + "]" + name);
        if (children != null) {
            for (Node child : children) {
                child.dump(ps, tab + "\t");
            }
        }
    }

    protected void require(boolean cond, String... str) {
        if (!cond)
            error(str);
    }

    protected void error(String... str) {
        throw new AssertionError(fullName() + ": " + String.join(",", str));
    }
}
