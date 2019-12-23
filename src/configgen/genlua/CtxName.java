package configgen.genlua;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class CtxName {

    private final Set<String> locals = new HashSet<>();
    private final Map<String, String> fullNameToLocals = new TreeMap<>(); //用TreeMap使得生成代码确定

    private static int MAX_LOCAL = 128;

    static {
        String max_local = System.getProperty("genlua.max_local");
        if (max_local != null) {
            MAX_LOCAL = Integer.parseInt(max_local);
        }
    }

    Map<String, String> getLocalNameMap() {
        return fullNameToLocals;
    }

    String getLocalName(String fullName) {
        String loc = fullNameToLocals.get(fullName);
        if (loc != null) {
            return loc;
        }

        // 因为lua有local变量总共250个左右的限制,这里限制128给其他留一点;luajit没这个限制
        // 参考https://zhuanlan.zhihu.com/p/31732401
        // https://stackoverflow.com/questions/38952744/lua-ellipsis-expression-limited-at-248
        if (fullNameToLocals.size() > MAX_LOCAL) {
            return fullName;
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

        return fullName;
    }
}
