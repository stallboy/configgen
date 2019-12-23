package configgen.genlua;

import configgen.define.Bean;
import configgen.value.*;

import java.util.*;

class ValueStringify implements ValueVisitor {

    private static Set<String> keywords = new HashSet<>(Arrays.asList(
            "break", "goto", "do", "end", "for", "in", "repeat", "util", "while",
            "if", "then", "elseif", "function", "local", "nil", "true", "false"));


    static void getLuaString(StringBuilder res, String value) {
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
    private Ctx ctx;
    private String beanTypeStr;
    private boolean isKey;

    private ValueStringify key;
    private ValueStringify notKey;

    ValueStringify(StringBuilder res, Ctx ctx, String beanTypeStr) {
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

    private ValueStringify(StringBuilder res, Ctx ctx, boolean isKey) {
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
        if (AContext.getInstance().getLangSwitch() != null && !isKey && value.getType().hasText()) { // text字段仅用于asValue，不能用于asKey
            int id = AContext.getInstance().getLangSwitch().enterText(value.value) + 1;
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
            res.append("''");
//            res.append("\"").append(val).append("\"");
        }
    }

    @Override
    public void visit(VList value) {
        int sz = value.getList().size();
        if (sz == 0) { //优化，避免重复创建空table
            res.append(ctx.getCtxShared().getEmptyTableName());

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
            return ctx.getCtxShared().getSharedName(value); //优化，重用相同的table
        }
        return null;
    }

    @Override
    public void visit(VMap value) {
        int sz = value.getMap().size();
        if (sz == 0) { //优化，避免重复创建空table
            res.append(ctx.getCtxShared().getEmptyTableName());

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
            beanType = ctx.getCtxName().getLocalName(Name.fullName(val.getTBean()));
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
                boolean meetBool = false;
                boolean doPack = AContext.getInstance().isPack() && (val.getTBean().getBoolFieldCount() > 1);
                for (Value fieldValue : val.getValues()) {
                    if (doPack && fieldValue instanceof VBool) { //从第一个遇到的bool开始搞
                        if (!meetBool) {
                            meetBool = true;

                            BitSet bs = new BitSet();
                            int cnt = 0;
                            for (Value fv : val.getValues()) {
                                if (fv instanceof VBool) {
                                    VBool fbv = (VBool) fv;
                                    if (fbv.value) {
                                        bs.set(cnt);
                                    }
                                    cnt++;
                                }
                            }
                            idx += cnt;
                            AContext.getInstance().getStatistics().usePackBool(cnt - 1);

                            long v = 0;
                            if (bs.length() > 0) {
                                v = bs.toLongArray()[0];
                            }
                            res.append("0x").append(Long.toHexString(v));
                            if (idx != sz) {
                                res.append(", ");
                            }


                        }
                    } else {
                        idx++;
                        fieldValue.accept(notKey);
                        if (idx != sz) {
                            res.append(", ");
                        }
                    }


                }
                res.append(")");
            }
        }

    }
}
