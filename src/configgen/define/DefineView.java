package configgen.define;

import configgen.Node;
import configgen.type.AllType;

import java.util.*;

public class DefineView extends Node {
    private final String own;
    private final boolean ignoreExcludes; //是否忽略exclude，在构造fullDefine时需要忽略

    // defineView 抽取的结果
    public final Map<String, Bean> beans = new TreeMap<>();
    public final Map<String, Table> tables = new TreeMap<>();

    DefineView(AllDefine define, String own) {
        this(define, own, false);
    }

    private DefineView(AllDefine define, String own, boolean ignoreExcludes) {
        super(define, "defineView(" + own + ")");

        this.own = own;
        this.ignoreExcludes = ignoreExcludes;

        define.extract(this, own);

        trimUnusedBeans();

        resolveExtract();
    }

    static DefineView fullDefine(AllDefine define) {
        String own = ""; // own所有
        //ignoreExcludes = true; //不排除任何table
        return new DefineView(define, own, true);
    }

    void resolveExtract() {
        for (Bean bean : beans.values()) {
            try {
                bean.resolveExtract(this);
            } catch (Throwable e) {
                throw new AssertionError(bean.name + ",解析这个结构体抽取部分出错", e);
            }
        }
        for (Table table : tables.values()) {
            try {
                table.resolveExtract(this);
            } catch (Throwable e) {
                throw new AssertionError(table.name + ",解析这个表结构抽取部分出错", e);
            }
        }
    }

    void trimUnusedBeans() {
        Set<String> actuallyUsedBeans = new TreeSet<>(); //实际被使用的Bean，通过遍历table获取
        for (Table table : tables.values()) {
            resolveUsedBean(table, actuallyUsedBeans);
        }

        for (String beanName : new ArrayList<>(beans.keySet())) {
            if (!actuallyUsedBeans.contains(beanName)) {
                beans.remove(beanName);
            }
        }
    }

    private void resolveUsedBean(Table table, Set<String> dstUsedBeans) {
        resolveUsedBean(table.bean, dstUsedBeans);
    }

    private void resolveUsedBeanIf(Bean bean, Set<String> dstUsedBeans) {
        if (bean != null && beans.containsKey(bean.name) && dstUsedBeans.add(bean.name)) {
            resolveUsedBean(bean, dstUsedBeans);
        }
    }

    private void resolveUsedBean(Bean bean, Set<String> dstUsedBeans) {
        if (bean.type == Bean.BeanType.BaseDynamicBean) {
            for (Bean actionBean : bean.childDynamicBeans.values()) {
                resolveUsedBean(actionBean, dstUsedBeans);
            }
        } else {
            for (Column col : bean.columns.values()) {
                resolveUsedBean(col, dstUsedBeans);
            }
        }
    }

    private void resolveUsedBean(Column col, Set<String> dstUsedBeans) {
        if (col.type.startsWith("list,")) {
            String[] sp = col.type.split(",");
            String v = sp[1].trim();

            resolveUsedBeanIf(beans.get(v), dstUsedBeans);
        } else if (col.type.startsWith("map,")) {
            String[] sp = col.type.split(",");
            String k = sp[1].trim();
            String v = sp[2].trim();

            resolveUsedBeanIf(beans.get(k), dstUsedBeans);
            resolveUsedBeanIf(beans.get(v), dstUsedBeans);
        } else {
            resolveUsedBeanIf(beans.get(col.type), dstUsedBeans);
        }
    }

    boolean isOwn(String defineOwn) {
        if (own == null || own.isEmpty()) {
            return true;
        }
        if (defineOwn == null || defineOwn.isEmpty()) {
            return false;
        }
        for (String oneOwn : defineOwn.split(",")) {
            if (own.equals(oneOwn)) {
                return true;
            }
        }
        return false;
    }

    boolean isIgnoreExcludes() {
        return ignoreExcludes;
    }

    AllType buildAllType() {
        AllType allType = new AllType(this);
        allType.resolve();

        return allType;
    }

}
