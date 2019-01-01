package configgen.value;

import configgen.type.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Value {
    public final Type type;

    Value(Type type) {
        this.type = type;
    }

    public abstract void accept(ValueVisitor visitor);

    public abstract void verifyConstraint();

    public abstract boolean isCellEmpty();

    public abstract void collectCells(List<Cell> cells);

    void verifyRefs() {
        for (SRef sref : type.constraint.references) {
            if (isCellEmpty()) {
                require(sref.refNullable, "有空格子，则外键必须是nullable的", sref.refTable);
            } else {
                if (sref.cache == null){
                    VTable vtable = VDb.getCurrent().getVTable(sref.refTable.name);
                    sref.cache = sref.refToPrimaryKey() ? vtable.primaryKeyValueSet : vtable.uniqueKeyValueSetMap.get(String.join(",", sref.refCols));
                }
                require(sref.cache.contains(this), "外键未找到", sref.refTable);
            }
        }
    }


    void require(boolean cond, Object... args) {
        if (!cond)
            error(args);
    }

    void error(Object... args) {
        throw new AssertionError(join(args) + " -- " + toString());
    }


    private String join(Object... args) {
        return Arrays.stream(args).map(Objects::toString).collect(Collectors.joining(","));
    }

    public static Value create(Type t, List<Cell> data) {
        return t.accept(new TypeVisitorT<Value>() {
            @Override
            public Value visit(TBool type) {
                return new VBool(type, data);
            }

            @Override
            public Value visit(TInt type) {
                return new VInt(type, data);
            }

            @Override
            public Value visit(TLong type) {
                return new VLong(type, data);
            }

            @Override
            public Value visit(TFloat type) {
                return new VFloat(type, data);
            }

            @Override
            public Value visit(TString type) {
                return new VString(type, data);
            }

            @Override
            public Value visit(TList type) {
                return new VList(type, data);
            }

            @Override
            public Value visit(TMap type) {
                return new VMap(type, data);
            }

            @Override
            public Value visit(TBean type) {
                return new VBean(type, data);
            }
        });
    }
}
