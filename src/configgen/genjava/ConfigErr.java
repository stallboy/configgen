package configgen.genjava;

public class ConfigErr extends RuntimeException {

    public ConfigErr(Exception e) {
        super(e);
    }

    public ConfigErr(String s) {
        super(s);
    }
}
