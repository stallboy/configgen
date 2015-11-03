package configgen.gen;

public interface Provider {
    Generator create(Parameter parameter);

    String usage();
}
