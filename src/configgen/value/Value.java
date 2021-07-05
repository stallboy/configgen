package configgen.value;

import configgen.type.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Value {
    protected final Type type;

    Value(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public abstract void accept(ValueVisitor visitor);

    /**
     * 检验约束
     * 约束分两种，一是Ref，一是Range
     */
    public abstract void verifyConstraint();

    public abstract boolean isCellEmpty();

    public abstract void collectCells(List<Cell> cells);

    void verifyRefs() {
        for (SRef sref : type.getConstraint().references) {
            if (isCellEmpty()) {
                require(sref.refNullable, "有空格子，则外键必须是nullable的", sref.refTable);
            } else {
                if (sref.cache == null) {
                    VTable vtable = AllValue.getCurrent().getVTable(sref.refTable.name);
                    sref.cache = sref.refToPrimaryKey() ? vtable.primaryKeyValueSet : vtable.uniqueKeyValueSetMap.get(String.join(",", sref.refCols));
                }
                if (type.isPrimitiveAndTableKey()) {
                    //noinspection StatementWithEmptyBody
                    if (sref.refNullable) {
                        //主键，并且nullableRef，--->则可以格子中有值，但ref不到
                    } else {
                        require(sref.cache.contains(this), "外键未找到", sref.refTable);
                    }
                } else { //非主键，格子中有值，--->则就算配置为nullableRef也不行
                    require(sref.cache.contains(this), "外键未找到", sref.refTable);
                }
            }
        }
    }


    void require(boolean cond, Object... args) {
        if (!cond)
            error(args);
    }

    void error(Object... args) {
        throw new AssertionError(join(args) + " -- " + this);
    }


    private String join(Object... args) {
        return Arrays.stream(args).map(Objects::toString).collect(Collectors.joining(","));
    }

}
