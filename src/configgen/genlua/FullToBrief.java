package configgen.genlua;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class FullToBrief {
    private final Map<String, String> usedFullNameToBriefNames = new TreeMap<>(); //用TreeMap使得生成代码确定
    private final Set<String> forbidBriefNames = new HashSet<>();
    private final Set<String> usedBriefNames = new HashSet<>();

    FullToBrief(String pkg) {
        forbidBriefNames.add(pkg);
        forbidBriefNames.add("Beans");
        forbidBriefNames.add("this");
        forbidBriefNames.add("mk");
    }

    void clear() {
        usedBriefNames.clear();
        usedFullNameToBriefNames.clear();
    }

    Map<String, String> getAll() {
        return usedFullNameToBriefNames;
    }

    String toBrief(String fullName) {
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
