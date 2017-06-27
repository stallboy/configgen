package configgen.genjava;

public interface VisitorT<T> {
    T visit(SchemaPrimitive schemaPrimitive);

    T visit(SchemaRef schemaRef);

    T visit(SchemaList schemaList);

    T visit(SchemaMap schemaMap);

    T visit(SchemaBean schemaBean);

    T visit(SchemaInterface schemaInterface);

    T visit(SchemaEnum schemaEnum);
}
