package configgen.value;

import configgen.type.*;

@SuppressWarnings("ALL")
public class Values {
    public static Value create(Type t, AData<?> adata) {
        return t.accept(new TypeVisitorT<Value>() {
            @Override
            public Value visit(TBool type) {
                return new VBool(type, adata.cells);
            }

            @Override
            public Value visit(TInt type) {
                return new VInt(type, adata.cells);
            }

            @Override
            public Value visit(TLong type) {
                return new VLong(type, adata.cells);
            }

            @Override
            public Value visit(TFloat type) {
                return new VFloat(type, adata.cells);
            }

            @Override
            public Value visit(TString type) {
                return new VString(type, adata.cells);
            }

            @Override
            public Value visit(TList type) {
                return new VList(type, (AData<TList>) adata);
            }

            @Override
            public Value visit(TMap type) {
                return new VMap(type, (AData<TMap>) adata);
            }

            @Override
            public Value visit(TBean type) {
                throw new AssertionError("VBean直接由VTable创建,创建子Bean的话类型是TBeanRef");
            }

            @Override
            public Value visit(TBeanRef type) {
                AData<TBean> newAData = new AData<>(adata.cells, ((TBeanRef) adata.fullType).tBean, adata.isCompressAsOne());
                return new VBean(type.tBean, newAData);
            }
        });
    }
}
