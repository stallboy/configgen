package configgen.type;

import configgen.define.Column;

import java.util.Objects;

public class TList extends Type {
    public final Type value;
    public final int count; // >=1
    public final Column.PackType packType;
    public final char packSeparator;

    TList(TBean parent, String name, int idx, String value, int count, Column.PackType packType, char packSeparator) {
        super(parent, name, idx);

        this.value = resolveType(parent, "value", idx, value, packType == Column.PackType.AsOne);
        require(Objects.nonNull(this.value), "list里的值类型不存在", value);
        this.count = count;
        this.packType = packType;
        this.packSeparator = packSeparator;
    }

    @Override
    void setConstraint(Constraint cons) {
        super.setConstraint(cons);
        require(cons.range == null, "list不支持Range");
        for (SRef sref : cons.references) {
            require(!sref.refNullable, "list不支持nullableRef");
            require(null == sref.mapKeyRefTable, "list不支持keyRef");
        }
        value.setConstraint(cons);
    }

    @Override
    public String toString() {
        if (packType == Column.PackType.NoPack) {
            return String.format("list,%s,%d", value, count);
        } else {
            return String.format("list,%s", value);
        }
    }

    @Override
    public <T> T accept(TypeVisitorT<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean hasRef() {
        return value.hasRef();
    }

    @Override
    public boolean hasSubBean() {
        return value instanceof TBeanRef;
    }

    @Override
    public boolean hasText() {
        return value.hasText();
    }

    @Override
    public boolean hasBlock() {
        return packType == Column.PackType.Block || value.hasBlock();
    }

    @Override
    public int columnSpan() {
        switch (packType) {
            case NoPack:
                return value.columnSpan() * count;
            case Block:
                return value.columnSpan();
            default:
                return 1;
        }
    }

}
