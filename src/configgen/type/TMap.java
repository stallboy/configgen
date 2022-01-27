package configgen.type;

import java.util.Objects;

public class TMap extends Type {
    public final Type key;
    public final Type value;
    public final int count; // >=1
    public final boolean isPackByBlock;

    TMap(TBean parent, String name, int idx, String key, String value, int count, boolean block) {
        super(parent, name, idx);
        this.key = resolveType(parent, "key", idx, key, false);
        require(Objects.nonNull(this.key), "map的Key类型不存在", key);
        if (this.key instanceof TString) {
            require(!this.key.hasText(), "map的Key类型不能是Text");
        }
        this.value = resolveType(parent, "value", idx, value, false);
        require(Objects.nonNull(this.value), "map的Value类型不存在", value);
        if (this.value instanceof TString) {
            require(!this.value.hasText(), "map的Value类型不能是Text");
        }
        this.count = count;
        this.isPackByBlock = block;
    }

    @Override
    void setConstraint(Constraint cons) {
        super.setConstraint(cons);
        require(cons.range == null, "map不支持range");
        Constraint kc = new Constraint();
        Constraint vc = new Constraint();
        for (SRef sref : cons.references) {
            require(!sref.refNullable, "map不支持nullableRef");
            if (null != sref.mapKeyRefTable)
                kc.references.add(new SRef(sref.mapKeyRefTable, sref.mapKeyRefCols));
            if (null != sref.refTable)
                vc.references.add(new SRef(sref.refTable, sref.refCols));
        }
        key.setConstraint(kc);
        value.setConstraint(vc);
    }

    @Override
    public String toString() {
        if (isPackByBlock) {
            return String.format("map,%s,%s", key, value);
        } else {
            return String.format("map,%s,%s,%d", key, value, count);
        }
    }

    @Override
    public <T> T accept(TypeVisitorT<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean hasRef() {
        return key.hasRef() || value.hasRef();
    }

    @Override
    public boolean hasSubBean() {
        return key instanceof TBeanRef || value instanceof TBeanRef;
    }

    @Override
    public boolean hasText() {
        return key.hasText() || value.hasText();
    }

    @Override
    public boolean hasBlock() {
        return isPackByBlock || key.hasBlock() || value.hasBlock();
    }

    @Override
    public int columnSpan() {
        return (key.columnSpan() + value.columnSpan()) * count;
    }

}
