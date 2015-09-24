package configgen.type;

public interface Visitor {
    void visit(TBool type);

    void visit(TInt type);

    void visit(TLong type);

    void visit(TFloat type);

    void visit(TString type);

    void visit(TText type);

    void visit(TList type);

    void visit(TMap type);

    void visit(TBean type);
}


