package configgen.genjava;

public interface Visitor {
    void visit(SchemaPrimitive s);

    void visit(SchemaBean s);

    void visit(SchemaList s);

    void visit(SchemaMap s);
}
