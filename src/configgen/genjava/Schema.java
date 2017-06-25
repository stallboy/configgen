package configgen.genjava;

public interface Schema {
    boolean compatible(Schema other);

    void accept(Visitor visitor);

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
                SchemaRef sr = new SchemaRef();
                sr.read(input);
                return sr;

            case LIST:
                SchemaList sl = new SchemaList();
                sl.read(input);
                return sl;
            case MAP:
                SchemaMap sm = new SchemaMap();
                sm.read(input);
                return sm;

            case BEAN:
                SchemaBean sb = new SchemaBean();
                sb.read(input);
                return sb;
            case INTERFACE:
                SchemaInterface si = new SchemaInterface();
                si.read(input);
                return si;

            case ENUM:
                SchemaEnum se = new SchemaEnum();
                se.read(input);
                return se;

            default:
                throw new ConfigErr("schema tag " + tag + " not supported");
        }
    }
}
