package configgen.value;

public interface ValueVisitor {
    void visit(VBool value);

    void visit(VInt value);

    void visit(VLong value);

    void visit(VFloat value);

    void visit(VString value);

    void visit(VList value);

    void visit(VMap value);

    void visit(VBean value);
}


