package configgen.value;

import configgen.util.ListParser;
import configgen.util.NestListParser;

import java.util.List;
import java.util.stream.Collectors;

public class Cells {

    public static List<Cell> parseFunc(Cell dat) {
        return NestListParser.parseFunction(dat.getData()).stream().map(dat::createSub).collect(Collectors.toList());
    }

    public static List<Cell> parseNestList(Cell dat) {
        return NestListParser.parseNestList(dat.getData()).stream().map(dat::createSub).collect(Collectors.toList());
    }

    public static List<Cell> parseList(Cell dat, char separator) {
        return ListParser.parseList(dat.getData(), separator).stream().map(dat::createSub).collect(Collectors.toList());
    }
}
