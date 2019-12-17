package configgen.genlua;

import configgen.value.*;

import java.util.*;

public class ValueContext {
    private static final Set<String> forbidBriefNames = new HashSet<>();
    private static int allEmptyTableUseCount = 0;
    private static boolean useShared;
    private static String emptyTableStr;

    public static void init(String pkg, boolean _useShared) {
        useShared = _useShared;
        if (useShared) {
            emptyTableStr = "E";
        } else {
            emptyTableStr = "{}";
        }
        forbidBriefNames.add(pkg);
        forbidBriefNames.add("Beans");
        forbidBriefNames.add("A"); //用于表示共享Table
        forbidBriefNames.add("E"); //用于表示emptyTable
        forbidBriefNames.add("this");
        forbidBriefNames.add("mk");
    }

    public static boolean isUseShared() {
        return useShared;
    }

    public static int getAllEmptyTableUseCount() {
        return allEmptyTableUseCount;
    }

    public static String getEmptyTableStr() {
        return emptyTableStr;
    }


    public static class VCompositeStr {
        private String valueStr;
        private String briefName;

        public VCompositeStr(int i) {
            briefName = String.format("A[%d]", i);
        }

        public void setValueStr(String value) {
            valueStr = value;
        }

        public String getBriefName() {
            return briefName;
        }

        public String getValueStr() {
            return valueStr;
        }
    }

    ////////////////////////////////////////////per vTable

    private final Map<String, String> usedFullNameToBriefNames = new TreeMap<>(); //用TreeMap使得生成代码确定
    private final Set<String> usedBriefNames = new HashSet<>();
    private int emptyTableUseCount = 0;

    // 用于lua生成时table能共享内存就共享，以最小化客户端的内存占用
    private final Map<VComposite, VCompositeStr> sharedCompositeValues = new LinkedHashMap<>();

    public ValueContext(VTable vTable) {
        if (useShared) {
            VCompositeCollector col = new VCompositeCollector();
            for (VBean vBean : vTable.getVBeanList()) {
                vBean.accept(col);
            }

            int i = 0;
            for (VCompositeCollector.VCompositeCnt vc : col.getCompositeValueToCnt().values()) {
                if (vc.getCnt() > 1) {
                    i++;
                    sharedCompositeValues.put(vc.getFirst(), new VCompositeStr(i));
                }
            }
        }
    }

    public void stringifySharedCompositeValues() {
        if (useShared) {
            useShared = false;

            for (Map.Entry<VComposite, VCompositeStr> entry : sharedCompositeValues.entrySet()) {
                StringBuilder sb = new StringBuilder();
                entry.getKey().accept(new ValueStringify(sb, this, null));
                entry.getValue().setValueStr(sb.toString());
            }

            useShared = true;
        }
    }

    public int getEmptyTableUseCount() {
        return emptyTableUseCount;
    }

    public Map<String, String> getFullNameToBriefNameMap() {
        return usedFullNameToBriefNames;
    }

    public Collection<VCompositeStr> getSharedCompositeStrs() {
        return sharedCompositeValues.values();
    }


    public VCompositeStr getSharedVCompositeStr(VComposite v) {
        return sharedCompositeValues.get(v);
    }

    void useEmptyTable() {
        emptyTableUseCount++;
        allEmptyTableUseCount++;
    }

    String getBriefName(String fullName) {
        String brief = usedFullNameToBriefNames.get(fullName);
        if (brief != null) {
            return brief;
        }

        String[] seps = fullName.split("\\.");
        String tryName = null;
        for (int i = seps.length - 1; i >= 0; i--) {
            if (tryName == null) {
                tryName = seps[i];
            } else {
                tryName = seps[i] + "_" + tryName;
            }

            if (forbidBriefNames.contains(tryName)) {
                continue;
            }

            if (usedBriefNames.contains(tryName)) {
                continue;
            }

            usedBriefNames.add(tryName);
            usedFullNameToBriefNames.put(fullName, tryName);
            return tryName;
        }

        throw new RuntimeException("竟然找不到个不重复的名字，我选择死亡");
    }
}
