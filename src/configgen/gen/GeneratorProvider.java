package configgen.gen;

public interface GeneratorProvider {
    Generator create(Parameter parameter);
}
