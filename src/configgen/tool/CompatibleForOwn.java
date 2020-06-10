package configgen.tool;

import configgen.Logger;
import configgen.define.AllDefine;
import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.Table;

import java.nio.file.Path;

public class CompatibleForOwn {


    public static void makeCompatible(Path xmlFile, String encoding) {
        AllDefine fullDefine = new AllDefine(xmlFile, encoding);
        Logger.mm("define");


        for (Bean bean : fullDefine.getAllBeans()) {
            for (String own : bean.own.split(",")) {
                if (noColumnOwn(bean, own)) {
                    makeAllColumnOwn(bean, own);
                }
            }
        }

        for (Table table : fullDefine.getAllTables()) {
            for (String own : table.bean.own.split(",")) {
                if (noColumnOwn_Normal(table.bean, own)) {
                    makeAllColumnOwn_Normal(table.bean, own);
                }
            }
        }

        fullDefine.save();
    }

    private static boolean noColumnOwn(Bean bean, String own) {
        if (bean.type == Bean.BeanType.BaseDynamicBean) {
            return noColumnOwn_Dynamic(bean, own);
        } else {
            return noColumnOwn_Normal(bean, own);
        }
    }

    private static void makeAllColumnOwn(Bean bean, String own) {
        if (bean.type == Bean.BeanType.BaseDynamicBean) {
            makeAllColumnOwn_Dynamic(bean, own);
        } else {
            makeAllColumnOwn_Normal(bean, own);
        }
    }


    private static boolean noColumnOwn_Normal(Bean bean, String own) {
        for (Column col : bean.columns.values()) {
            if (col.own.contains(own)) {
                return false;
            }
        }
        return true;
    }

    private static void makeAllColumnOwn_Normal(Bean bean, String own) {
        for (Column col : bean.columns.values()) {
            if (col.own.trim().isEmpty()) {
                col.own = own;
            } else {
                col.own = String.format("%s,%s", col.own.trim(), own);
            }
        }
    }

    private static boolean noColumnOwn_Dynamic(Bean bean, String own) {
        for (Bean c : bean.childDynamicBeans.values()) {
            if (!noColumnOwn_Normal(c, own)) {
                return false;
            }
        }
        return true;
    }

    private static void makeAllColumnOwn_Dynamic(Bean bean, String own) {
        for (Bean c : bean.childDynamicBeans.values()) {
            makeAllColumnOwn_Normal(c, own);
        }
    }

}
