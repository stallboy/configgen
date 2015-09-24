package configgen.type;

public interface TVisitor<T> {
    T visit(TBool type);

    T visit(TInt type);

    T visit(TLong type);

    T visit(TFloat type);

    T visit(TString type);

    T visit(TText type);

    T visit(TList type);

    T visit(TMap type);

    T visit(TBean type);
}


