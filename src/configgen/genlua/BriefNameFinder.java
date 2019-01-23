package configgen.genlua;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class BriefNameFinder {
    final Map<String, String> usedFullNameToBriefNames = new HashMap<>();
    private final Set<String> forbidBriefNames = new HashSet<>();
    private final Set<String> usedBriefNames = new HashSet<>();

    BriefNameFinder(String pkg) {
        forbidBriefNames.add(pkg);
        forbidBriefNames.add("Beans");
        forbidBriefNames.add("this");
        forbidBriefNames.add("mk");
    }

    void clear() {
        usedBriefNames.clear();
        usedFullNameToBriefNames.clear();
    }

    String findBriefName(String fullName) {
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
