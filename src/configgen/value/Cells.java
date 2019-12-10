package configgen.value;

import configgen.util.ListParser;
import configgen.util.NestListParser;

import java.util.List;
import java.util.stream.Collectors;

public class Cells {

    public static List<Cell> parseFunc(Cell dat) {
        return NestListParser.parseFunction(dat.data).stream().
                map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
    }

    public static List<Cell> parseNestList(Cell dat) {
        return NestListParser.parseNestList(dat.data).stream().
                map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
    }

    public static List<Cell> parseList(Cell dat, char separator) {
        return ListParser.parseList(dat.data, separator).stream().
                map(s -> new Cell(dat.row, dat.col, s)).collect(Collectors.toList());
    }
}
