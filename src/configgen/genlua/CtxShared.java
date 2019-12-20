package configgen.genlua;

import configgen.value.VComposite;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

class CtxShared {

    private int emptyTableUseCount = 0;
    private final Map<VComposite, VCompositeStr> sharedCompositeValues = new LinkedHashMap<>();

    // 用于lua生成时table能共享内存就共享，以最小化客户端的内存占用
    static class VCompositeStr {
        private String valueStr = null;
        private String name;

        VCompositeStr(int i) {
            name = String.format("A[%d]", i);
        }

        void setValueStr(String value) {
            valueStr = value;
        }

        String getName() {
            return name;
        }

        String getValueStr() {
            return valueStr;
        }
    }


    void parseShared(Ctx ctx) {
        ValueShared shared = new ValueShared(ctx.getVTable());
        shared.iterateShared();
        int idx = 0;
        for (int i = shared.getLayers().size() - 1; i >= 0; i--) {
            ValueSharedLayer layer = shared.getLayers().get(i);
            for (ValueSharedLayer.VCompositeCnt vc : layer.getCompositeValueToCnt().values()) {
                if (vc.getCnt() > 1) {
                    idx++;
                    AContext.getInstance().getStatistics().useSharedTable(vc.getCnt() - 1);
                    sharedCompositeValues.put(vc.getFirst(), new VCompositeStr(idx));
                }
            }
        }

        for (Map.Entry<VComposite, VCompositeStr> entry : sharedCompositeValues.entrySet()) {
            StringBuilder sb = new StringBuilder();
            entry.getKey().accept(new ValueStringify(sb, ctx, null));
            entry.getValue().setValueStr(sb.toString());
        }
    }


    Collection<VCompositeStr> getSharedList() {
        return sharedCompositeValues.values();
    }

    String getSharedName(VComposite v) {
        VCompositeStr vstr = sharedCompositeValues.get(v);
        if (vstr != null) {
            if (vstr.getValueStr() != null) {
                return vstr.getName();
            } else { //这个用于stringifySharedCompositeValues
                return null;
            }
        }
        return null;
    }


    int getEmptyTableUseCount() {
        return emptyTableUseCount;
    }

    String getEmptyTableName() {
        emptyTableUseCount++;
        AContext.getInstance().getStatistics().useEmptyTable();
        return AContext.getInstance().getEmptyTableStr();
    }


}
