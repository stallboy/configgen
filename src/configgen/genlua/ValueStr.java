package configgen.genlua;

import configgen.define.Bean;
import configgen.value.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ValueStr {
    private static Set<String> keywords = new HashSet<>(Arrays.asList("break", "goto", "do", "end", "for", "in", "repeat", "util", "while", "if", "then", "elseif", "function", "local", "nil", "true", "false"));

    static void getLuaValueString(StringBuilder res, Value thisValue, BriefNameFinder finder, String beanTypeStr, boolean asKey) {
        thisValue.accept(new ValueVisitor() {

            private void add(String val) {
                if (asKey) {
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
                String val = value.value.replace("\r\n", "\\n");
                val = val.replace("\n", "\\n");
                val = val.replace("\"", "\\\"");
                if (asKey) {
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
                int idx = 0;
                res.append("{");
                for (Value eleValue : value.getList()) {
                    getLuaValueString(res, eleValue, finder, null, false);
                    idx++;
                    if (idx != sz) {
                        res.append(", ");
                    }
                }
                res.append("}");
            }

            @Override
            public void visit(VMap value) {
                int sz = value.getMap().size();
                int idx = 0;

                res.append("{");
                for (Map.Entry<Value, Value> entry : value.getMap().entrySet()) {
                    getLuaValueString(res, entry.getKey(), finder, null, true);
                    res.append(" = ");
                    getLuaValueString(res, entry.getValue(), finder, null, false);
                    idx++;
                    if (idx != sz) {
                        res.append(", ");
                    }
                }
                res.append("}");
            }

            @Override
            public void visit(VBean value) {
                VBean val = value;
                if (value.getTBean().getBeanDefine().type == Bean.BeanType.BaseDynamicBean) {
                    val = value.getChildDynamicVBean();
                }
                String beanType = beanTypeStr;
                if (beanType == null) {
                    beanType = finder.findBriefName(Name.fullName(val.getTBean()));
                }

                res.append(beanType).append("(");

                int sz = val.getValues().size();
                int idx = 0;
                for (Value fieldValue : val.getValues()) {
                    getLuaValueString(res, fieldValue, finder, null, false);
                    idx++;
                    if (idx != sz) {
                        res.append(", ");
                    }
                }
                res.append(")");
            }
        });
    }
}
