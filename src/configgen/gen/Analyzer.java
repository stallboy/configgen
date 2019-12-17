package configgen.gen;

import configgen.data.AllData;
import configgen.define.AllDefine;
import configgen.type.*;
import configgen.value.AllValue;

import java.util.ArrayList;
import java.util.List;

public class Analyzer {


    public static void analyze(Context ctx) {
        AllDefine ownDefine = ctx.getFullData().getFullDefine().extract("client");
        AllType ownType = new AllType(ownDefine);
        ownType.resolve();

        List<TBean> beans = new ArrayList<>();
        for (TTable tTable : ownType.getTTables()) {
            beans.add(tTable.getTBean());
        }

        beans.addAll(ownType.getTBeans());
        for (TBean tBean : ownType.getTBeans()) {
            beans.addAll(tBean.getChildDynamicBeans());
        }

        int beanCnt = 0;
        for (TBean bean : beans) {
            int cnt = 0;
            for (Type column : bean.getColumns()) {
                if (column instanceof TBool) {
                    cnt++;
                }
            }

            if (cnt > 1) {
                beanCnt++;
                System.out.printf("%s, %d\n", bean.name, cnt);
            }

        }
        System.out.printf("共有%d个结构\n", beanCnt);

    }

}
