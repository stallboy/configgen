package configgen.value;

import configgen.type.*;

import java.util.List;

public class Values {
    public static Value create(Type t, List<Cell> cells, Type fullType, boolean packAsOne) {
        return t.accept(new TypeVisitorT<Value>() {
            @Override
            public Value visit(TBool type) {
                return new VBool(type, cells);
            }

            @Override
            public Value visit(TInt type) {
                return new VInt(type, cells);
            }

            @Override
            public Value visit(TLong type) {
                return new VLong(type, cells);
            }

            @Override
            public Value visit(TFloat type) {
                return new VFloat(type, cells);
            }

            @Override
            public Value visit(TString type) {
                return new VString(type, cells);
            }

            @Override
            public Value visit(TList type) {
                return new VList(type, new AData<>(cells, (TList) fullType, packAsOne));
            }

            @Override
            public Value visit(TMap type) {
                return new VMap(type, new AData<>(cells, (TMap) fullType, packAsOne));
            }

            @Override
            public Value visit(TBean type) {
                throw new AssertionError("VBean直接由VTable创建,创建子Bean的话类型是TBeanRef");
            }

            @Override
            public Value visit(TBeanRef type) {
                AData<TBean> newAData = new AData<>(cells, ((TBeanRef) fullType).tBean, packAsOne || type.packAsOne);
                return new VBean(type.tBean, newAData);
            }
        });
    }
}
