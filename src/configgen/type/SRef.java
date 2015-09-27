package configgen.type;

public class SRef {
    public final String name;
    public final Cfg ref;
    public final boolean nullable;
    public final Cfg keyRef;

    public SRef(String name, Cfg ref, boolean nullable, Cfg keyRef) {
        this.name = name;
        this.ref = ref;
        this.nullable = nullable;
        this.keyRef = keyRef;
    }
}
