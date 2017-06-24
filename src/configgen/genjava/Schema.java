package configgen.genjava;

public interface Schema {
    boolean compatible(Schema other);

    void accept(Visitor visitor);

    void write(ConfigOutput output);

    void readExtra(ConfigInput input);

    static Schema read(ConfigInput input) {
        int tag = input.readInt();
        switch (tag) {
            case 1:
                return SchemaPrimitive.SBool;
            case 2:
                return SchemaPrimitive.SInt;
            case 3:
                return SchemaPrimitive.SLong;
            case 4:
                return SchemaPrimitive.SFloat;
            case 5:
                return SchemaPrimitive.SStr;
            case 6:
                Schema bs = new SchemaBean();
                bs.readExtra(input);
                return bs;
            case 7:
                Schema ls = new SchemaList();
                ls.readExtra(input);
                return ls;
            case 8:
                Schema ms = new SchemaMap();
                ms.readExtra(input);
                return ms;
            default:
                throw new ConfigErr("schema tag " + tag + " not supported");
        }
    }
}
