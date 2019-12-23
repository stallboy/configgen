package configgen.gen;

public interface Parameter {
    String get(String key, String def, String info);

    boolean has(String key, String info);

    void end();
}
