package configgen.genlua;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class CtxName {

    private final Set<String> locals = new HashSet<>();
    private final Map<String, String> fullNameToLocals = new TreeMap<>(); //用TreeMap使得生成代码确定


    Map<String, String> getLocalNameMap() {
        return fullNameToLocals;
    }

    String getLocalName(String fullName) {
        String loc = fullNameToLocals.get(fullName);
        if (loc != null) {
            return loc;
        }

        String[] seps = fullName.split("\\.");
        String tryName = null;
        for (int i = seps.length - 1; i >= 0; i--) {
            if (tryName == null) {
                tryName = seps[i];
            } else {
                tryName = seps[i] + "_" + tryName;
            }

            if (AContext.getInstance().isForbidName(tryName)) {
                continue;
            }

            if (locals.contains(tryName)) {
                continue;
            }

            locals.add(tryName);
            fullNameToLocals.put(fullName, tryName);
            return tryName;
        }

        throw new RuntimeException("竟然找不到个不重复的名字，我选择死亡");
    }
}
