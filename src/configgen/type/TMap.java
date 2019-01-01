package configgen.type;

import configgen.Node;

import java.util.Objects;

public class TMap extends Type {
    public final Type key;
    public final Type value;
    public final int count; // must > 0

    TMap(Node parent, String name, Constraint cons, String key, String value, int count) {
        super(parent, name, cons);
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

        this.key = resolveType("key", kc, key);
        require(Objects.nonNull(this.key), "map的Key类型不存在", key);
        this.value = resolveType("value", vc, value);
        require(Objects.nonNull(this.value), "map的Value类型不存在", value);
        this.count = count;
    }

    @Override
    public String toString() {
        return "map," + key + "," + value + "," + count;
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
        return key.hasRef() || value.hasRef();
    }

    @Override
    public boolean hasSubBean() {
        return key instanceof TBean || value instanceof TBean;
    }

    @Override
    public boolean hasText() {
        return key.hasText() || value.hasText();
    }

    @Override
    public int columnSpan() {
        return (key.columnSpan() + value.columnSpan()) * count;
    }

}
