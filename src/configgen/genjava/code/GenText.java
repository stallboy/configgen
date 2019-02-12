package configgen.genjava.code;

import configgen.gen.Generator;
import configgen.gen.LangSwitch;
import configgen.util.CachedIndentPrinter;

import java.util.stream.Collectors;

class GenText {

    static void generate(LangSwitch ls, CachedIndentPrinter ps) {
        ps.println("package %s;", Name.codeTopPkg);
        ps.println();

        ps.println("public class Text {");

        //fields
        for (LangSwitch.Lang lang : ls.getAllLangInfo()) {
            ps.println1("private String %s;", Generator.lower1(lang.getLang()));
        }

        //constructor
        ps.println1("private Text() {");
        ps.println1("}");
        ps.println();

        ps.println1("public Text(%s) {", ls.getAllLangInfo().stream().map(e -> "String " + Generator.lower1(e.getLang())).collect(Collectors.joining(", ")));
        for (LangSwitch.Lang lang : ls.getAllLangInfo()) {
            String langStr = Generator.lower1(lang.getLang());
            ps.println2("this.%s = %s;", langStr, langStr);
        }
        ps.println1("}");
        ps.println();

        ps.println1("public static Text _create(configgen.genjava.ConfigInput input) {");
        ps.println2("Text self = new Text();");
        for (LangSwitch.Lang lang : ls.getAllLangInfo()) {
            ps.println2("self.%s = input.readStr();", Generator.lower1(lang.getLang()));
        }
        ps.println2("return self;");
        ps.println1("}");
        ps.println();


        //getters
        for (LangSwitch.Lang lang : ls.getAllLangInfo()) {
            ps.println1("public String get%s() {", Generator.upper1(lang.getLang()));
            ps.println2("return %s;", Generator.lower1(lang.getLang()));
            ps.println1("}");
            ps.println();
        }

        ps.println("}");
    }
}
