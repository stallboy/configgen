package configgen.define;

import configgen.Node;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Collectors;

public class Bean extends Node {
    public final Map<String, Field> fields = new LinkedHashMap<>();
    public final List<Ref> refs = new ArrayList<>();
    public final List<ListRef> listRefs = new ArrayList<>();
    public final Map<String, Range> ranges = new HashMap<>();
    public final boolean compress;
    private final String own;

    public Bean(Node _parent, Element self) {
        super(_parent, self.getAttribute("name"));
        String[] attrs = DomUtils.attributes(self, "name", "own", "compress", "enum", "keys");
        own = attrs[1];
        compress = attrs[2].equalsIgnoreCase("true") || attrs[2].equals("1");
        if (compress) {
            require(_parent instanceof ConfigCollection, "config not allowed compress");
        }
        List<List<Element>> eles = DomUtils.elementsList(self, "field", "ref", "range", "listref");
        for (Element ef : eles.get(0)) {
            Field f = new Field(this, ef);
            require(null == fields.put(f.name, f), "field duplicate name=" + f.name);
        }
        refs.addAll(eles.get(1).stream().map(ec -> new Ref(this, ec)).collect(Collectors.toList()));
        for (Element ef : eles.get(2)) {
            Range r = new Range(this, ef);
            require(null == ranges.put(r.key, r), "range duplicate key=" + r.key);
        }
        listRefs.addAll(eles.get(3).stream().map(ec -> new ListRef(this, ec)).collect(Collectors.toList()));
    }

    public Bean(Config config, String name) {
        super(config, name);
        own = "";
        compress = false;
    }

    private Bean(Node _parent, Bean original) {
        super(_parent, original.name);
        own = original.own;
        compress = original.compress;
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

    Bean extract(Node _parent, String _own) {
        Bean part = new Bean(_parent, this);

        fields.forEach((name, f) -> {
            Field pf = f.extract(part, _own);
            if (pf != null)
                part.fields.put(name, pf);
        });
        if (part.fields.isEmpty() && own.contains(_own)) {
            fields.forEach((name, f) -> part.fields.put(name, new Field(part, f)));
        }

        if (part.fields.isEmpty())
            return null;

        ranges.forEach((n, r) -> {
            if (part.fields.containsKey(n))
                part.ranges.put(n, new Range(part, r));
        });

        part.refs.addAll(refs.stream().map(r -> new Ref(part, r)).collect(Collectors.toList()));
        part.listRefs.addAll(listRefs.stream().map(r -> new ListRef(part, r)).collect(Collectors.toList()));
        return part;
    }

    void resolveExtract() {
        fields.values().forEach(Field::resolveExtract);
        
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