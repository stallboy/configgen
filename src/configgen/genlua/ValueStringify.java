package configgen.genlua;

import configgen.define.Bean;
import configgen.gen.LangSwitch;
import configgen.value.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ValueStringify implements ValueVisitor {

    private static Set<String> keywords = new HashSet<>(Arrays.asList(
            "break", "goto", "do", "end", "for", "in", "repeat", "util", "while",
            "if", "then", "elseif", "function", "local", "nil", "true", "false"));
    private static LangSwitch langSwitch;

    public static void setLangSwitch(LangSwitch lang) {
        langSwitch = lang;
    }

    public static void getLuaString(StringBuilder res, String value) {
        String val = toLuaStringLiteral(value);
        res.append("\"").append(val).append("\"");
    }

    private static String toLuaStringLiteral(String value) {
        String val = value.replace("\r\n", "\\n");
        val = val.replace("\n", "\\n");
        val = val.replace("\"", "\\\"");
        return val;
    }

    //////////////////////////////////////////// per vtable

    private StringBuilder res;
    private ValueContext ctx;
    private String beanTypeStr;
    private boolean isKey;

    private ValueStringify key;
    private ValueStringify notKey;

    public ValueStringify(StringBuilder res, ValueContext ctx, String beanTypeStr) {
        this.res = res;
        this.ctx = ctx;
        this.beanTypeStr = beanTypeStr;
        this.isKey = false;

        key = new ValueStringify(res, ctx, true);
        notKey = new ValueStringify(res, ctx, false);
        key.key = key;
        key.notKey = notKey;
        notKey.key = key;
        notKey.notKey = notKey;
    }

    private ValueStringify(StringBuilder res, ValueContext ctx, boolean isKey) {
        this.res = res;
        this.ctx = ctx;
        this.beanTypeStr = null;
        this.isKey = isKey;
    }


    private void add(String val) {
        if (isKey) {
            res.append('[').append(val).append(']');
        } else {
            res.append(val);
        }
    }

    @Override
    public void visit(VBool value) {
        add(value.value ? "true" : "false");
    }

    @Override
    public void visit(VInt value) {
        add(String.valueOf(value.value));
    }

    @Override
    public void visit(VLong value) {
        add(String.valueOf(value.value));
    }

    @Override
    public void visit(VFloat value) {
        add(String.valueOf(value.value));
    }

    @Override
    public void visit(VString value) {
        if (langSwitch != null && !isKey && value.getType().hasText()) { // text字段仅用于asValue，不能用于asKey
            int id = langSwitch.enterText(value.value) + 1;
            res.append(id);
            return;
        }

        String val = toLuaStringLiteral(value.value);
        if (isKey) {
            if (keywords.contains(val) || val.contains("-") || val.contains("=") || val.contains(",")) {
                res.append("[\"").append(val).append("\"]");
            } else {
                res.append(val);
            }
        } else {
            res.append("\"").append(val).append("\"");
        }
    }

    @Override
    public void visit(VList value) {
        int sz = value.getList().size();
        if (sz == 0) { //优化，避免重复创建空table
            ctx.useEmptyTable();
            res.append(ValueContext.getEmptyTableStr());

        } else {
            String vstr = getSharedCompositeBriefName(value);
            if (vstr != null) { //优化，重用相同的table
                res.append(vstr);

            } else {
                int idx = 0;
                res.append("{");
                for (Value eleValue : value.getList()) {
                    eleValue.accept(notKey);
                    idx++;
                    if (idx != sz) {
                        res.append(", ");
                    }
                }
                res.append("}");
            }
        }
    }

    private String getSharedCompositeBriefName(VComposite value) {
        if (value.isShared()) {
            return ctx.getSharedVCompositeBriefName(value); //优化，重用相同的table
        }
        return null;
    }

    @Override
    public void visit(VMap value) {
        int sz = value.getMap().size();
        if (sz == 0) { //优化，避免重复创建空table
            ctx.useEmptyTable();
            res.append(ValueContext.getEmptyTableStr());

        } else {
            String vstr = getSharedCompositeBriefName(value);
            if (vstr != null) { //优化，重用相同的table
                res.append(vstr);

            } else {
                int idx = 0;
                res.append("{");
                for (Map.Entry<Value, Value> entry : value.getMap().entrySet()) {
                    entry.getKey().accept(key);
                    res.append(" = ");
                    entry.getValue().accept(notKey);
                    idx++;
                    if (idx != sz) {
                        res.append(", ");
                    }
                }
                res.append("}");
            }
        }
    }

    @Override
    public void visit(VBean value) {
        VBean val = value;
        if (value.getTBean().getBeanDefine().type == Bean.BeanType.BaseDynamicBean) {
            val = value.getChildDynamicVBean();
        }
        String beanType = beanTypeStr;
        if (beanType == null) {
            beanType = ctx.getBriefName(Name.fullName(val.getTBean()));
        }

        String vstr = getSharedCompositeBriefName(value);
        if (vstr != null) { //优化，重用相同的table
            res.append(vstr);

        } else {
            res.append(beanType);
            int sz = val.getValues().size();
            if (sz > 0) { // 这里来个优化，如果没有参数不加()，因为beanType其实直接就是个实例
                res.append("(");
                int idx = 0;
                for (Value fieldValue : val.getValues()) {
                    fieldValue.accept(notKey);
                    idx++;
                    if (idx != sz) {
                        res.append(", ");
                    }
                }
                res.append(")");
            }
        }

    }
}
