package configgen.genjava;

public interface Schema {
    /**
     * 比较兼容性，codeSchema.compatible(dataSchema);
     * 参照example/javaload/LoadConfig.java
     */
    boolean compatible(Schema other);

    void accept(Visitor visitor);

    <T> T accept(VisitorT<T> visitor);

    void write(ConfigOutput output);

    int BOOL = 1;
    int INT = 2;
    int LONG = 3;
    int FLOAT = 4;
    int STR = 5;
    int REF = 6;
    int LIST = 7;
    int MAP = 8;
    int BEAN = 9;
    int INTERFACE = 10;
    int ENUM = 11;

    static Schema create(ConfigInput input) {
        int tag = input.readInt();
        switch (tag) {
            case BOOL:
                return SchemaPrimitive.SBool;
            case INT:
                return SchemaPrimitive.SInt;
            case LONG:
                return SchemaPrimitive.SLong;
            case FLOAT:
                return SchemaPrimitive.SFloat;
            case STR:
                return SchemaPrimitive.SStr;
            case REF:
                return new SchemaRef(input);

            case LIST:
                return new SchemaList(input);
            case MAP:
                return new SchemaMap(input);

            case BEAN:
                return new SchemaBean(input);
            case INTERFACE:
                return new SchemaInterface(input);
            case ENUM:
                return new SchemaEnum(input);

            default:
                throw new ConfigErr("schema tag " + tag + " not supported");
        }
    }
}
