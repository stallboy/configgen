package configgen.define;

import configgen.Node;
import configgen.Utils;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Collectors;

public class Bean extends Node {
    public final String name; // a.b.c
    public final String own;
    public final boolean compress;

    public final Map<String, Field> fields = new LinkedHashMap<>();
    public final List<Ref> refs = new ArrayList<>();
    public final List<ListRef> listRefs = new ArrayList<>();
    public final Map<String, Range> ranges = new HashMap<>();

    public Bean(ConfigCollection root, Config config, Element self) {
        super(config != null ? config : root, "");

        String[] attrs = Utils.attributes(self, "name", "own", "compress", "enum", "keys");
        name = attrs[0];
        if (config == null)
            link = name;

        own = attrs[1];
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
        own = "";
        compress = false;
    }

    public void save(Element parent) {
        update(Utils.newChild(parent, "bean"));
    }

    void update(Element self) {
        self.setAttribute("name", name);
        if (!own.isEmpty())
            self.setAttribute("own", own);
        if (compress)
            self.setAttribute("compress", "true");

        fields.values().forEach(f -> f.save(self));
        refs.forEach(c -> c.save(self));
        listRefs.forEach(c -> c.save(self));
        ranges.values().forEach(c -> c.save(self));
    }
}