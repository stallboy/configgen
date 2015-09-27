package configgen.value;

import java.io.IOException;

public interface ValueVisitor {
    void visit(VBool value) throws IOException;

    void visit(VInt value) throws IOException;

    void visit(VLong value) throws IOException;

    void visit(VFloat value) throws IOException;

    void visit(VString value) throws IOException;

    void visit(VText value) throws IOException;

    void visit(VList value) throws IOException;

    void visit(VMap value) throws IOException;

    void visit(VBean value);
}


