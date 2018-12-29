package configgen.type;

import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.ForeignKey;
import configgen.define.KeyRange;

import java.util.*;

public class TBean extends Type {
    public final Bean beanDefine;

    public final Map<String, Type> columns = new LinkedHashMap<>();
    private final List<TForeignKey> foreignKeys = new ArrayList<>();
    public final List<TForeignKey> mRefs = new ArrayList<>();
    public final List<TForeignKey> listRefs = new ArrayList<>();
    private final Set<String> refNames = new HashSet<>(); //make sure generate ok

    public TTable actionEnumRefTable;
    public final Map<String, TBean> actionBeans = new LinkedHashMap<>();


    public TBean(TDb parent, Bean bean) {
        super(parent, bean.name, new Constraint());
        beanDefine = bean;
        if (beanDefine.type == Bean.BeanType.NormalBean) {
            init();
        } else {
            beanDefine.actionBeans.forEach((n, b) -> actionBeans.put(n, new TBean(this, b)));
        }
    }

    public TBean(TTable parent, Bean bean) {
        super(parent, bean.name, new Constraint());
        beanDefine = bean;
        init();
    }

    public TBean(TBean parent, Bean bean) {
        super(parent, bean.name, new Constraint());
        beanDefine = bean;
        init();
    }

    private void init() {
        for (ForeignKey fk : beanDefine.foreignKeys.values()) {
            require(refNames.add(fk.name), "ref name conflict for generate", fk.name);
            foreignKeys.add(new TForeignKey(this, fk));
        }
        for (Column col : beanDefine.columns.values()) {
            ForeignKey fk = col.foreignKey;
            if (fk != null) {
                require(refNames.add(fk.name), "ref name conflict for generate", fk.name);
                foreignKeys.add(new TForeignKey(this, fk));
            }
        }
    }


    public int getColumnIndex(String col) {
        return columns.get(col).indexAtBean;
    }

    private boolean _hasRef = false;
    private boolean _hasRefChecked = false;

    private boolean _hasSubBean = false;
    private boolean _hasSubBeanChecked = false;

    private boolean _hasText = false;
    private boolean _hasTextChecked = false;

    private int _columnSpan = 0;
    private boolean _hasColumnSpanChecked = false;

    @Override
    public boolean hasRef() {
        if (!_hasRefChecked) {
            _hasRef = checkHasRef();
            _hasRefChecked = true;
        }
        return _hasRef;

    }

    @Override
    public boolean hasSubBean() {
        if (!_hasSubBeanChecked) {
            _hasSubBean = checkHasSubBean();
            _hasSubBeanChecked = true;
        }
        return _hasSubBean;
    }

    @Override
    public boolean hasText() {
        if (!_hasTextChecked) {
            _hasText = checkHasText();
            _hasTextChecked = true;
        }
        return _hasText;
    }

    @Override
    public int columnSpan() {
        if (!_hasColumnSpanChecked) {
            _columnSpan = checkColumnSpan();
            _hasColumnSpanChecked = true;
        }
        return _columnSpan;
    }

    @Override
    public String toString() {
        return beanDefine.name;
    }


    private boolean checkHasRef() {
        if (beanDefine.type == Bean.BeanType.BaseAction)
            return actionBeans.values().stream().anyMatch(TBean::hasRef);
        else
            return mRefs.size() > 0 || listRefs.size() > 0 || columns.values().stream().anyMatch(Type::hasRef);
    }


    private boolean checkHasSubBean() {
        if (beanDefine.type == Bean.BeanType.BaseAction)
            return actionBeans.values().stream().anyMatch(TBean::hasSubBean);
        else
            return columns.values().stream().anyMatch(t -> t.hasSubBean() || t instanceof TBean);
    }


    private boolean checkHasText() {
        if (beanDefine.type == Bean.BeanType.BaseAction)
            return actionBeans.values().stream().anyMatch(TBean::hasText);
        else
            return columns.values().stream().anyMatch(Type::hasText);
    }

    private int checkColumnSpan() {
        if (beanDefine.type == Bean.BeanType.BaseAction) {
            OptionalInt max = actionBeans.values().stream().mapToInt(TBean::columnSpan).max();
            if (max.isPresent()) {
                return max.getAsInt() + 1;
            } else {
                return 1;
            }
        } else {
            return beanDefine.compress ? 1 : columns.values().stream().mapToInt(Type::columnSpan).sum();
        }
    }


    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(TypeVisitorT<T> visitor) {
        return visitor.visit(this);
    }

    public void resolve() {
        if (beanDefine.type == Bean.BeanType.BaseAction) {
            actionEnumRefTable = ((TDb) root).ttables.get(beanDefine.actionEnumRef);
            require(actionEnumRefTable != null, "action enum ref table not found", beanDefine.actionEnumRef);
            actionBeans.values().forEach(TBean::resolve);
        } else {
            foreignKeys.forEach(TForeignKey::resolve);
            beanDefine.columns.values().forEach(this::resolveColumn);
            require(columns.size() > 0, "has no columns");
            foreignKeys.forEach(fk -> {
                if (fk.foreignKeyDefine.refType == ForeignKey.RefType.LIST)
                    listRefs.add(fk);
                else if (fk.foreignKeyDefine.keys.length > 1)
                    mRefs.add(fk);
            });
        }
    }

    private void resolveColumn(Column col) {
        Constraint cons = new Constraint();
        foreignKeys.forEach(fk -> {
            if (fk.foreignKeyDefine.refType != ForeignKey.RefType.LIST && fk.foreignKeyDefine.keys.length == 1 && fk.foreignKeyDefine.keys[0].equals(col.name))
                cons.references.add(new SRef(fk));
        });

        if (null != col.keyRange) {
            cons.range = col.keyRange.range;
        }
        KeyRange kr = beanDefine.ranges.get(col.name);
        if (kr != null) {
            require(cons.range == null, "range allow one per column");
            cons.range = kr.range;
        }

        String t, k = "", v = "";
        int c = 0;
        char compressSeparator = ';';
        if (col.type.startsWith("list,")) {
            t = "list";
            String[] sp = col.type.split(",");
            v = sp[1].trim();
            if (sp.length > 2) {
                c = Integer.parseInt(sp[2].trim());
                require(c >= 1);
            }
            if (c == 0) {
                require(col.compress, "count=0 list must has compress attribute");
                compressSeparator = col.compressSeparator;
            }
        } else if (col.type.startsWith("map,")) {
            t = "map";
            String[] sp = col.type.split(",");
            k = sp[1].trim();
            v = sp[2].trim();
            c = Integer.parseInt(sp[3].trim());
            require(c >= 1);
        } else {
            t = col.type;
        }

        Type type = resolveType(col.name, cons, t, k, v, c, compressSeparator);
        if (type != null) {
            type.indexAtBean = columns.size();
            columns.put(col.name, type);
        } else {
            error("type resolve err", col.type);
        }
    }
}