package configgen.type;

import configgen.define.Column;

import java.util.Objects;

public class TList extends Type {
    public final Type value;
    /**
     * >=0; 0 意味着这个是compress的，使用compressSeparator分割
     */
    public final int count;
    public final Column.PackType packType;
    public final char compressSeparator;

    TList(TBean parent, String name, int idx, String value, int count, Column.PackType packType, char compressSeparator) {
        super(parent, name, idx);

        this.value = resolveType(parent, "value", idx, value, packType == Column.PackType.AsOne);
        require(Objects.nonNull(this.value), "list里的值类型不存在", value);
        this.count = count;
        this.packType = packType;
        this.compressSeparator = compressSeparator;
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
        return "list," + value + (count > 0 ? "," + count : "");
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
    public int columnSpan() {
        return packType != Column.PackType.NoPack ? 1 : (value.columnSpan() * count);
    }

}
