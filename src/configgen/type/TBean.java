package configgen.type;

import configgen.Node;
import configgen.define.*;

import java.util.*;

public class TBean extends Type {
    public final Bean define;
    public final Map<String, Type> fields = new LinkedHashMap<>();
    public final List<KeysRef> keysRefs = new ArrayList<>();
    public final List<ListRef> listRefs = new ArrayList<>();

    public TBean(Cfgs parent, Bean bean) {
        super(parent, bean.name, new Constraint());
        this.define = bean;
        init();
    }

    public TBean(Cfg parent, Bean bean) {
        super(parent, "", new Constraint());
        this.define = bean;

        init();
    }

    public TBean(Node parent, String link, Constraint cons, TBean source) {
        super(parent, link, cons);
        Assert(source != null);
        define = source.define;
        source.fields.forEach((k, v) -> fields.put(k, v.copy(this)));
        source.keysRefs.forEach(v -> keysRefs.add(v.copy(this)));
        source.listRefs.forEach(v -> listRefs.add(v.copy(this)));
    }

    @Override
    public Type copy(Node parent) {
        return new TBean(parent, link, constraint, this);
    }

    @Override
    public boolean hasRef() {
        return keysRefs.size() > 0 || listRefs.size() > 0 || fields.values().stream().filter(Type::hasRef).count() > 0;
    }

    @Override
    public boolean hasText() {
        return fields.values().stream().filter(Type::hasText).count() > 0;
    }

    @Override
    public int columnSpan() {
        return define.compress ? 1 : fields.values().stream().mapToInt(Type::columnSpan).sum();
    }

    @Override
    public String toString() {
        return define.name;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(TVisitor<T> visitor) {
        return visitor.visit(this);
    }

    private void init() {
        for (Ref r : define.refs) {
            if (r.keys.length > 1) {
                keysRefs.add(new KeysRef(this, r));
            }
        }

        for (configgen.define.ListRef r : define.listRefs) {
            listRefs.add(new ListRef(this, r));
        }
    }

    void resolve() {
        for (Field f : define.fields.values()) {
            fields.put(f.name, resolveType(f));
        }

        keysRefs.forEach(KeysRef::resolve);
        listRefs.forEach(ListRef::resolve);

        Assert(fields.size() > 0, "has no fields");
        if (define.compress) {
            fields.values().forEach(t -> Assert(t instanceof TPrimitive, "compress field must be primitive"));
        }
    }

    private Type resolveType(Field f) {
        String t, k = "", v = "";
        int c = 0;
        if (f.type.startsWith("list,")) {
            t = "list";
            String[] sp = f.type.split(",");
            v = sp[1].trim();
            if (sp.length > 2) {
                c = Integer.parseInt(sp[2].trim());
                Assert(c >= 1);
            }
        } else if (f.type.startsWith("map,")) {
            t = "map";
            String[] sp = f.type.split(",");
            k = sp[1].trim();
            v = sp[2].trim();
            c = Integer.parseInt(sp[3].trim());
            Assert(c >= 1);
        } else {
            t = f.type;
        }

        Constraint cons = new Constraint();
        resolveConstraint(cons, f.ref, f.nullableRef, f.keyRef, f.range);
        for (Ref r : define.refs) {
            if (r.keys.length == 1 && r.keys[0].equals(f.name)) {
                resolveConstraint(cons, r.ref, r.nullableRef, r.keyRef, "");
            }
        }
        configgen.define.Range rg = define.ranges.get(f.name);
        if (rg != null) {
            addConstraintRange(cons, new Range(rg.min, rg.max));
        }

        Type res = Type.resolve(this, f.name, cons, t, k, v, c);
        f.Assert(res != null, "type resolve err", f.type);
        return res;

    }

    private void resolveConstraint(Constraint cons, String ref, String nullableRef, String keyRef, String range) {
        Cfgs cfgs = (Cfgs) root;
        if (!ref.isEmpty()) {
            Cfg c = cfgs.cfgs.get(ref);
            Assert(c != null, "ref not found", ref);
            cons.refs.add(c);
        }

        if (!nullableRef.isEmpty()) {
            Cfg c = cfgs.cfgs.get(nullableRef);
            Assert(c != null, "nullableRef not found", nullableRef);
            cons.nullableRefs.add(c);
        }

        if (!keyRef.isEmpty()) {
            Cfg c = cfgs.cfgs.get(keyRef);
            Assert(c != null, "keyRef not found", keyRef);
            cons.keyRefs.add(c);
        }

        if (!range.isEmpty()) {
            String[] sp = range.split(",");
            int min = Integer.decode(sp[0]);
            int max = Integer.decode(sp[1]);
            Assert(max > min, "range.max must > range.min");
            addConstraintRange(cons, new Range(min, max));
        }
    }

    private void addConstraintRange(Constraint cons, Range r) {
        Assert(cons.range == null, "range allow one per field");
        cons.range = r;
    }

}