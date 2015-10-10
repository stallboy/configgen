package configgen.define;

import configgen.Node;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Collectors;

public class Bean extends Node {
    public final String name; // a.b.c
    private final String own;
    public final boolean compress;

    public final Map<String, Field> fields = new LinkedHashMap<>();
    public final List<Ref> refs = new ArrayList<>();
    public final List<ListRef> listRefs = new ArrayList<>();
    public final Map<String, Range> ranges = new HashMap<>();

    public Bean(ConfigCollection collection, Config config, Element self) {
        super(config != null ? config : collection, "");

        String[] attrs = DomUtils.attributes(self, "name", "own", "compress", "enum", "keys");
        name = attrs[0];
        if (config == null)
            link = name;

        own = attrs[1];
        compress = attrs[2].equalsIgnoreCase("true") || attrs[2].equals("1");
        if (compress) {
            Assert(config == null, "config not allowed compress");
        }

        List<List<Element>> eles = DomUtils.elementsList(self, "field", "ref", "range", "listref");
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

    private Bean(ConfigCollection collection, Config config, Bean original, Map<String, Field> ownFields) {
        super(config != null ? config : collection, original.link);
        name = original.name;
        own = original.name;
        compress = original.compress;
        fields.putAll(ownFields);

        original.ranges.forEach((n, r) -> {
            if (fields.containsKey(n))
                ranges.put(n, r);
        });

        refs.addAll(original.refs); //wait extract2 to delete
        listRefs.addAll(original.listRefs);
    }

    public void save(Element parent) {
        update(DomUtils.newChild(parent, "bean"));
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

    Bean extract(ConfigCollection root, Config config, String own) {
        Map<String, Field> ownFields = new LinkedHashMap<>();
        fields.forEach((name, f) -> {
            if (f.own.contains(own))
                ownFields.put(name, f);
        });

        if (ownFields.isEmpty()) {
            if (this.own.contains(own))
                ownFields.putAll(fields);
            else
                return null;
        }

        return new Bean(root, config, this, ownFields);
    }

    void extract2() {
        List<Ref> dr = new ArrayList<>();
        refs.forEach(r -> {
            if (!fields.keySet().containsAll(Arrays.asList(r.keys)))
                dr.add(r);

            if (!r.ref.isEmpty() && !((ConfigCollection) root).configs.containsKey(r.ref))
                dr.add(r);

            if (!r.keyRef.isEmpty() && !((ConfigCollection) root).configs.containsKey(r.keyRef))
                dr.add(r);
        });

        refs.removeAll(dr);

        List<ListRef> dl = new ArrayList<>();
        listRefs.forEach(r -> {
            if (!fields.keySet().containsAll(Arrays.asList(r.keys)))
                dl.add(r);

            Config ref = ((ConfigCollection) root).configs.get(r.ref);
            if (null == ref || !ref.bean.fields.keySet().containsAll(Arrays.asList(r.refKeys)))
                dl.add(r);
        });
        listRefs.removeAll(dl);

    }
}