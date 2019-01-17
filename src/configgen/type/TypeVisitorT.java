package configgen.type;

public interface TypeVisitorT<T> {
    T visit(TBool type);

    T visit(TInt type);

    T visit(TLong type);

    T visit(TFloat type);

    T visit(TString type);

    T visit(TList type);

    T visit(TMap type);

    T visit(TBean type);

    T visit(TBeanRef type);
}


