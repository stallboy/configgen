package configgen.view;

import configgen.Node;
import configgen.define.*;
import configgen.type.AllType;

import java.util.*;

public class DefineView extends Node {
    public final ViewFilter filter;

    // defineView 抽取的结果
    public final Map<String, Bean> beans = new TreeMap<>();
    public final Map<String, Table> tables = new TreeMap<>();

    public DefineView(AllDefine define, ViewFilter filter) {
        super(define, filter.name());

        this.filter = filter;

        define.extract(this);

        trimUnusedBeans();

        resolveExtract();

        filter.saveToXml(define);
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

    private void resolveUsedBean(Bean bean, Set<String> dstUsedBeans) {
        if (bean.type == Bean.BeanType.BaseDynamicBean) {
            for (Bean actionBean : bean.childDynamicBeans.values()) {
                resolveUsedBean(actionBean, dstUsedBeans);
            }
        } else {
            for (Column col : bean.columns.values()) {
                resolveUsedBean(bean, col, dstUsedBeans);
            }
        }
    }

    private void resolveUsedBean(Bean parentBean, Column col, Set<String> dstUsedBeans) {
        if (col.type.startsWith("list,")) {
            String[] sp = col.type.split(",");
            String v = sp[1].trim();

            resolveUsedBeanIf(parentBean, v, dstUsedBeans);
        } else if (col.type.startsWith("map,")) {
            String[] sp = col.type.split(",");
            String k = sp[1].trim();
            String v = sp[2].trim();

            resolveUsedBeanIf(parentBean, k, dstUsedBeans);
            resolveUsedBeanIf(parentBean, v, dstUsedBeans);
        } else {
            resolveUsedBeanIf(parentBean, col.type, dstUsedBeans);
        }
    }

    private void resolveUsedBeanIf(Bean parentBean, String type, Set<String> dstUsedBeans) {
        if (isPrimitive(type)) {
            return;
        }

        String fullName = Ref.resolveBeanFullName(parentBean, type);
        Bean bean = beans.get(fullName);
        if (bean == null) {
            throw new AssertionError("bean: [" + fullName + "]被使用，但没有被包含进来，看是不是忘了设置own");
        }

        if (dstUsedBeans.add(bean.name)) {
            resolveUsedBean(bean, dstUsedBeans);
        }
    }

    private static boolean isPrimitive(String type) {
        switch (type) {
            case "int":
            case "long":
            case "string":
            case "bool":
            case "float":
            case "text":
                return true;
            default:
                return false;
        }
    }

    public AllType buildAllType() {
        AllType allType = new AllType(this);
        allType.resolve();

        return allType;
    }

}
