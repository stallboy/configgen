package configgen.type;

import configgen.Node;
import configgen.define.Column;

import java.util.Objects;

public class TList extends Type {
    public final Type value;
    public final int count; // >=0; 0 意味着这个是compress的，使用compressSeparator分割
    public final Column.CompressType compressType;
    public final char compressSeparator;

    TList(Node parent, String name, int idx, Constraint cons, String value, int count, Column.CompressType compressType, char compressSeparator) {
        super(parent, name, idx, cons);
        require(cons.range == null, "list不支持Range");
        for (SRef sref : cons.references) {
            require(!sref.refNullable, "list不支持nullableRef");
            require(null == sref.mapKeyRefTable, "list不支持keyRef");
        }
        this.value = resolveType("value", idx, cons, value, compressType == Column.CompressType.AsOne);
        require(Objects.nonNull(this.value), "list里的值类型不存在", value);
        this.count = count;
        this.compressType = compressType;
        this.compressSeparator = compressSeparator;
    }


    @Override
    public String toString() {
        return "list," + value + (count > 0 ? "," + count : "");
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
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
        return compressType != Column.CompressType.NoCompress ? 1 : (value.columnSpan() * count);
    }

}
