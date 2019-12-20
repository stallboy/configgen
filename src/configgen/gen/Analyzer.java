package configgen.gen;

import configgen.data.AllData;
import configgen.data.DTable;
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

        int columnStoreCnt = 0;
        for (TTable tTable : ownType.getTTables()) {
            DTable dTable = ctx.getFullData().getDTable(tTable.name);
            int dSize = dTable.getRecordList().size();
            int cSize = tTable.getTBean().getColumns().size();
            if (dSize - cSize > 100) {
                columnStoreCnt++;
                System.out.printf("%s: column=%d, data=%d\n", dTable.name, cSize, dSize);
            }
        }
        System.out.printf("共可用列存储数为%d\n", columnStoreCnt);

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
