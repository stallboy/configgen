package configgen.type;

import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.ForeignKey;
import configgen.define.KeyRange;

import java.util.*;

public class TBean extends Type {
    private final Bean beanDefine;

    // 列
    private final Map<String, Type> columns = new LinkedHashMap<>();

    // 可以有多个外键，foreignKeys包含所有外键信息。
    // 然后把单列外键分配到Type.constraints.references，多列外键分配到mRefs，
    // 索引到非unique key的表的外键分配到listRefs。
    private final List<TForeignKey> foreignKeys = new ArrayList<>();
    private final List<TForeignKey> mRefs = new ArrayList<>();
    private final List<TForeignKey> listRefs = new ArrayList<>();

    // 多态Bean基类包含这些子类定义
    private TTable childDynamicBeanEnumRefTable;
    private final Map<String, TBean> childDynamicBeans = new LinkedHashMap<>();


    public TBean(AllType parent, Bean bean) {
        super(parent, bean.name, -1);
        beanDefine = bean;
        if (beanDefine.type == Bean.BeanType.NormalBean) {
            init();
        } else {
            beanDefine.childDynamicBeans.forEach((n, b) -> childDynamicBeans.put(n, new TBean(this, b)));
        }
    }

    public TBean(TTable parent, Bean bean) {
        super(parent, bean.name, -1);
        beanDefine = bean;
        init();
    }

    public TBean(TBean parent, Bean bean) {
        super(parent, bean.name, -1);
        beanDefine = bean;
        init();
    }

    private void init() {
        Set<String> refNames = new HashSet<>();
        for (ForeignKey fk : beanDefine.foreignKeys.values()) {
            require(refNames.add(fk.name), "外键名字重复", fk.name);
            foreignKeys.add(new TForeignKey(this, fk));
        }
        for (Column col : beanDefine.columns.values()) {
            ForeignKey fk = col.foreignKey;
            if (fk != null) {
                require(refNames.add(fk.name), "外键名字重复", fk.name);
                foreignKeys.add(new TForeignKey(this, fk));
            }
        }
    }


    public Bean getBeanDefine() {
        return beanDefine;
    }

    public Type getColumn(String col) {
        return columns.get(col);
    }

    public Map<String, Type> getColumnMap() {
        return columns;
    }

    public Collection<Type> getColumns() {
        return columns.values();
    }

    public List<TForeignKey> getMRefs() {
        return mRefs;
    }

    public List<TForeignKey> getListRefs() {
        return listRefs;
    }

    public TTable getChildDynamicBeanEnumRefTable() {
        return childDynamicBeanEnumRefTable;
    }

    public Collection<TBean> getChildDynamicBeans() {
        return childDynamicBeans.values();
    }

    public TBean getChildDynamicBeanByName(String name) {
        return childDynamicBeans.get(name);
    }

    @Override
    public String fullName() {
        if (parent instanceof TTable) {
            return parent.fullName();
        } else {
            return parent.fullName() + "." + name;
        }
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


    private boolean checking = false;

    private boolean checkHasRef() {
        if (checking) { //递归时候的处理
            return false;
        }
        checking = true;
        try {
            if (beanDefine.type == Bean.BeanType.BaseDynamicBean)
                return childDynamicBeans.values().stream().anyMatch(TBean::hasRef);
            else
                return mRefs.size() > 0 || listRefs.size() > 0 || columns.values().stream().anyMatch(Type::hasRef);
        } finally {
            checking = false;
        }
    }


    private boolean checkHasSubBean() {
        if (checking) { //递归时候的处理
            return false;
        }
        checking = true;
        try {
            if (beanDefine.type == Bean.BeanType.BaseDynamicBean)
                return childDynamicBeans.values().stream().anyMatch(TBean::hasSubBean);
            else
                return columns.values().stream().anyMatch(t -> t instanceof TBeanRef || t.hasSubBean());
        } finally {
            checking = false;
        }
    }


    private boolean checkHasText() {
        if (checking) { //递归时候的处理
            return false;
        }
        checking = true;
        try {
            if (beanDefine.type == Bean.BeanType.BaseDynamicBean)
                return childDynamicBeans.values().stream().anyMatch(TBean::hasText);
            else
                return columns.values().stream().anyMatch(Type::hasText);
        } finally {
            checking = false;
        }
    }

    private int checkColumnSpan() {
        if (checking) { //递归时候的处理
            throw new RuntimeException("使用递归Bean时候要使用compressAsOne来避免没法计算列数");
        }

        checking = true;
        try {
            if (beanDefine.type == Bean.BeanType.BaseDynamicBean) {
                OptionalInt max = childDynamicBeans.values().stream().mapToInt(TBean::columnSpan).max();
                if (max.isPresent()) {
                    return max.getAsInt() + 1;
                } else {
                    return 1;
                }
            } else {
                return beanDefine.compress ? 1 : columns.values().stream().mapToInt(Type::columnSpan).sum();
            }
        } finally {
            checking = false;
        }
    }

    @Override
    public <T> T accept(TypeVisitorT<T> visitor) {
        return visitor.visit(this);
    }

    public void resolve() {
        if (beanDefine.type == Bean.BeanType.BaseDynamicBean) {
            childDynamicBeanEnumRefTable = ((AllType) root).getTTable(beanDefine.childDynamicBeanEnumRef);
            require(childDynamicBeanEnumRefTable != null, "多态Bean的枚举表不存在", beanDefine.childDynamicBeanEnumRef);
            for (TBean tBean : childDynamicBeans.values()) {
                tBean.resolve();
            }
        } else {
            for (Column column : beanDefine.columns.values()) {
                resolveColumnType(column);
            }
            for (TForeignKey foreignKey : foreignKeys) {
                foreignKey.resolve();
            }
            for (Type columnType : columns.values()) {
                Column column = beanDefine.columns.get(columnType.name);
                resolveColumnConstraint(columnType, column);
            }

//            require(columns.size() > 0, "Bean列数不能为0");
            for (TForeignKey fk : foreignKeys) {
                if (fk.foreignKeyDefine.refType == ForeignKey.RefType.LIST)
                    listRefs.add(fk);
                else if (fk.foreignKeyDefine.keys.length > 1)
                    mRefs.add(fk);
            }
        }
    }

    private void resolveColumnConstraint(Type columnType, Column col) {
        Constraint cons = new Constraint();
        for (TForeignKey fk : foreignKeys) {
            if (fk.foreignKeyDefine.refType != ForeignKey.RefType.LIST && fk.thisTableKeys.length == 1 && fk.thisTableKeys[0] == columnType)
                cons.references.add(new SRef(fk));
        }

        if (null != col.keyRange) {
            cons.range = col.keyRange.range;
        }
        KeyRange kr = beanDefine.ranges.get(col.name);
        if (kr != null) {
            require(cons.range == null, "一列只允许定义一个range", col.name);
            cons.range = kr.range;
        }
        columnType.setConstraint(cons);
    }

    private void resolveColumnType(Column col) {
        Type type;
        if (col.type.startsWith("list,")) {
            String[] sp = col.type.split(",");
            String v = sp[1].trim();
            int c = 0;
            if (sp.length > 2) {
                c = Integer.parseInt(sp[2].trim());
                require(c >= 1);
            }
            if (c == 0) {
                require(col.compressType == Column.CompressType.UseSeparator || col.compressType == Column.CompressType.AsOne,
                        "未定义列表的长度时必须定义compress或compressAsOne");
            }
            type = new TList(this, col.name, columns.size(), v, c, col.compressType, col.compressSeparator);

        } else if (col.type.startsWith("map,")) {
            String[] sp = col.type.split(",");
            String k = sp[1].trim();
            String v = sp[2].trim();
            int c = Integer.parseInt(sp[3].trim());
            require(c >= 1);
            require(c >= 1 && col.compressType == Column.CompressType.NoCompress,
                    "map必须配置长度，不支持配置compress或compressAsOne");
            type = new TMap(this, col.name, columns.size(), k, v, c);

        } else {
            type = resolveType(col.name, columns.size(), col.type, col.compressType == Column.CompressType.AsOne);
            if (type instanceof TPrimitive) {
                require(col.compressType == Column.CompressType.NoCompress,
                        "原始类型不要配置compress或compressAsOne");
            }
        }

        if (type != null) {
            columns.put(col.name, type);
        } else {
            error("类型不支持", col.type);
        }
    }

}