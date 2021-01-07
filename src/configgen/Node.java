package configgen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 这个类的目的，就是为了形成一颗树，这样
 * 1. 方便打印中间一个节点的fullName
 * 2. dump出整棵树，用于调试查看
 */
public class Node {
    protected Node root;
    public Node parent;
    public final String name;

    private List<Node> children;

    public Node(Node parent, String name) {
        this.name = name;
        _setParent(parent);
    }

    private void _setParent(Node parent) {
        this.parent = parent;
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

    @Override
    public String toString() {
        return fullName();
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

    protected void require(boolean cond, Object... args) {
        if (!cond)
            error(args);
    }

    protected void error(Object... args) {
        throw new AssertionError(fullName() + ": " + join(args));
    }

    private String join(Object... args) {
        return Arrays.stream(args).map(Objects::toString).collect(Collectors.joining(","));
    }
}
