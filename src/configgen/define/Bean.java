package configgen.define;

import configgen.Node;
import configgen.Utils;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Collectors;

public class Bean extends Node  {
    public final String name; // a.b.c
    public final String ownall;
    public final boolean compress;

    public final Map<String, Field> fields = new LinkedHashMap<>();
    public final  List<Ref> refs = new ArrayList<>();
    public final  List<ListRef> listRefs = new ArrayList<>();
    public final  Map<String, Range> ranges = new HashMap<>();

    public Bean(ConfigCollection root, Config config, Element self) {
        super(config != null ? config : root, "");

        String[] attrs = Utils.attrs(self, "name", "ownall", "compress", "tool", "enum", "keys");
        name = attrs[0];
        if (config == null)
            link = "[bean]" + name;

        ownall = attrs[1];
        compress = attrs[2].equalsIgnoreCase("true") || attrs[2].equals("1");
        if (compress) {
            Assert(config == null, "config not allowed compress");
        }

        List<List<Element>> eles = Utils.elementsList(self, "field", "ref", "range", "listref");
        for (Element ef : eles.get(0)) {
            Field f = new Field(this, ef);
            Assert(null == fields.put(f.name, f), "field duplicate name=" + f.name);
        }

        refs.addAll(eles.get(1).stream().map(ec -> new Ref(this, ec)).collect(Collectors.toList()));

        for (Element ef : eles.get(2)) {
            Range r = new Range(this, ef);
            Assert(null == ranges.put(r.key, r), "range duplicate key=" + r.key);
        }

        listRefs.addAll(eles.get(3).stream().map(ec -> new ListRef(this, ec)).collect(Collectors.toList()));
    }

    public Bean(Config config, String name) {
        super(config, "");
        this.name = name;
        ownall = "";
        compress = false;
    }
}