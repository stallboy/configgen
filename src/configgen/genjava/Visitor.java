package configgen.genjava;

public interface Visitor {
    void visit(SchemaPrimitive schemaPrimitive);

    void visit(SchemaRef schemaRef);

    void visit(SchemaList schemaList);

    void visit(SchemaMap schemaMap);

    void visit(SchemaBean schemaBean);

    void visit(SchemaInterface schemaInterface);

    void visit(SchemaEnum schemaEnum);
}
