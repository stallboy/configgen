package configgen.genlua;

import configgen.define.Column;
import configgen.gen.Generator;
import configgen.type.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class TypeStr {


    // uniqkeys : {{allname=, getname=, keyidx1=, keyidx2=}, }
    static String getLuaUniqKeysString(Ctx ctx) {
        TTable ttable = ctx.getVTable().getTTable();
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        sb.append(getLuaOneUniqKeyString(ctx, ttable.getPrimaryKey(), true));
        for (Map<String, Type> uniqueKey : ttable.getUniqueKeys()) {
            sb.append(getLuaOneUniqKeyString(ctx, uniqueKey, false));
        }
        sb.append("}");
        return sb.toString();
    }

    private static String getLuaOneUniqKeyString(Ctx ctx, Map<String, Type> keys, boolean isPrimaryKey) {
        String allname = isPrimaryKey ? "all" : Name.uniqueKeyMapName(keys);
        String getname = isPrimaryKey ? "get" : Name.uniqueKeyGetByName(keys);

        TTable ttable = ctx.getVTable().getTTable();
        Iterator<Type> it = keys.values().iterator();
        Type key1 = it.next();
        String keystr1 = getColumnStrOrIndex(key1, ttable.getTBean(), ctx.getCtxColumnStore().isUseColumnStore());

        if (keys.size() > 1) {
            if (keys.size() != 2) {
                throw new RuntimeException("uniqkeys size != 2 " + ttable.name);
            }
            Type key2 = it.next();
            String keystr2 = getColumnStrOrIndex(key2, ttable.getTBean(), ctx.getCtxColumnStore().isUseColumnStore());

            return String.format("{ '%s', '%s', %s, %s }, ", allname, getname, keystr1, keystr2);
        } else {
            return String.format("{ '%s', '%s', %s }, ", allname, getname, keystr1);
        }
    }

    private static String getColumnStrOrIndex(Type t, TBean bean, boolean isUseColumnStore) {
        if (isUseColumnStore) {
            return String.format("'%s'", Generator.lower1(t.name));
        } else {
            int idx = findColumnIndex(t, bean);
            return String.valueOf(idx);
        }
    }

    private static int findColumnIndex(Type t, TBean bean) {
        boolean doPack = isDoPackBool(bean);
        if (doPack) {
            boolean meetBool = false;
            int cnt = 0;
            for (Type column : bean.getColumns()) {
                if (column instanceof TBool) {
                    if (t.getColumnIndex() == column.getColumnIndex()) {
                        throw new RuntimeException("现在不支持packbool的同时，bool引用到起到表");
                    }
                    if (!meetBool) {
                        meetBool = true;
                        cnt++;
                    }
                } else {
                    cnt++;
                    if (t.getColumnIndex() == column.getColumnIndex()) {
                        return cnt;
                    }
                }
            }
            throw new IllegalStateException("不该发生");
        } else {
            return t.getColumnIndex() + 1;
        }
    }

    private static boolean isDoPackBool(TBean tbean) {
        boolean doPack = AContext.getInstance().isPack();
        if (doPack) {
            int boolCnt = tbean.getBoolFieldCount();
            if (boolCnt >= 50) {
                throw new RuntimeException("现在不支持pack多余50个bool字段的bean");
            }

            if (boolCnt < 2) {
                doPack = false;
            }
        }
        return doPack;
    }

    static String getLuaEnumString(Ctx ctx) {
        TTable ttable = ctx.getVTable().getTTable();
        Type enumCol = ttable.getEnumColumnType();

        if (enumCol == null) {
            return "nil";
        } else {
            return getColumnStrOrIndex(enumCol, ttable.getTBean(), ctx.getCtxColumnStore().isUseColumnStore());
        }
    }

    // refs { {refname, islist, dsttable, dstgetname, keyidx1, keyidx2}, }
    static String getLuaRefsString(TBean tbean, boolean isUseColumnStore) {
        StringBuilder sb = new StringBuilder();
        boolean hasRef = false;
        sb.append("{ ");

        for (Type t : tbean.getColumns()) {
            for (SRef r : t.getConstraint().references) {
                if (t instanceof TMap) {
                    System.out.println("map sref not suppport, bean=" + tbean.name);
                    break;
                }
                String refname = Name.refName(r);
                String dsttable = Name.fullName(r.refTable);
                String dstgetname = Name.uniqueKeyGetByName(r.refCols);
                String islist = "false";
                if (t instanceof TList) {
                    islist = "true";
                }
                String idx = getColumnStrOrIndex(t, tbean, isUseColumnStore);
                sb.append(String.format("\n    { '%s', %s, %s, '%s', %s }, ", refname, islist, dsttable, dstgetname, idx));
                hasRef = true;
            }
        }

        for (TForeignKey mRef : tbean.getMRefs()) {
            String refname = Name.refName(mRef);
            String dsttable = Name.fullName(mRef.refTable);
            String dstgetname = Name.uniqueKeyGetByName(mRef.foreignKeyDefine.ref.cols);

            String keyidx1 = getColumnStrOrIndex(mRef.thisTableKeys[0], tbean, isUseColumnStore);

            if (mRef.thisTableKeys.length > 1) {
                if (mRef.thisTableKeys.length != 2) {
                    throw new RuntimeException("keys length != 2 " + tbean.name);
                }
                String keyidx2 = getColumnStrOrIndex(mRef.thisTableKeys[1], tbean, isUseColumnStore);
                sb.append(String.format("\n    { '%s', false, %s, '%s', %s, %s }, ", refname, dsttable, dstgetname, keyidx1, keyidx2));
            } else {
                sb.append(String.format("\n    { '%s', false, %s, '%s', %s }, ", refname, dsttable, dstgetname, keyidx1));
            }

            hasRef = true;
        }
        sb.append("}");
        //忽略ListRef

        if (hasRef) {
            return sb.toString();
        } else {
            return "nil";
        }
    }

    static String getLuaFieldsString(TBean tbean, Ctx ctx) {
        StringBuilder sb = new StringBuilder();

        int cnt = tbean.getColumnMap().size();
        int i = 0;

        boolean useColumnStore = false;
        if (ctx != null) {
            useColumnStore = ctx.getCtxColumnStore().isUseColumnStore();
        }
        boolean doPack = (!useColumnStore) && isDoPackBool(tbean);
        boolean meetBool = false;

        for (Map.Entry<String, Type> entry : tbean.getColumnMap().entrySet()) {
            String n = entry.getKey();
            Type t = entry.getValue();

            if (doPack && t instanceof TBool) { //从第一个遇到的bool开始搞
                if (!meetBool) {
                    meetBool = true;

                    sb.append("\n    {");
                    for (Map.Entry<String, Type> be : tbean.getColumnMap().entrySet()) {
                        String bn = be.getKey();
                        Type bt = be.getValue();
                        if (bt instanceof TBool) {
                            i++;
                            Column f = tbean.getBeanDefine().columns.get(bn);
                            String c = f.desc.isEmpty() ? "" : ", " + f.desc;
                            sb.append("\n    '").append(Generator.lower1(bn)).append("', -- ").append(f.type).append(c);
                        }
                    }
                    if (i < cnt) {
                        sb.append("\n    },");
                    } else {
                        sb.append("\n    }");
                    }
                }

            } else { //正常的
                i++;
                String fieldName = String.format("'%s'", Generator.lower1(n));
                if (useColumnStore) {
                    PackInfo packInfo = ctx.getCtxColumnStore().getPackInfo(t.getColumnIndex());
                    if (packInfo != null) {
                        boolean isInt = t instanceof TInt;
                        if (isInt) {
                            fieldName = String.format("{%s, true, %d}", fieldName, packInfo.getBitLen());
                        } else {
                            fieldName = String.format("{%s, false}", fieldName);
                        }
                    }
                }
                Column f = tbean.getBeanDefine().columns.get(n);
                String c = f.desc.isEmpty() ? "" : ", " + f.desc;
                sb.append("\n    ").append(fieldName);

                if (i < cnt) {
                    sb.append(",");
                }
                sb.append(" -- ").append(f.type).append(c);
            }

        }

        return sb.toString();
    }

    static String getLuaFieldsStringEmmyLua(TBean tbean) {
        StringBuilder sb = new StringBuilder();
        for (String n : tbean.getColumnMap().keySet()) {
            Column f = tbean.getBeanDefine().columns.get(n);
            String c = f.desc.isEmpty() ? "" : ", " + f.desc;
            sb.append("---@field ").append(Generator.lower1(n)).append(" ").append(typeToLuaType(f.type)).append(" ").append(c).append("\n");
        }
        return sb.toString();
    }

    static String getLuaRefsStringEmmyLua(TBean tbean) {
        StringBuilder sb = new StringBuilder();
        boolean hasRef = false;

        for (Type t : tbean.getColumns()) {
            for (SRef r : t.getConstraint().references) {
                if (t instanceof TMap) {
                    System.out.println("map sref not suppport, bean=" + tbean.name);
                    break;
                }
                String refname = Name.refName(r);
                String dsttable = Name.fullName(r.refTable);
//                String dstgetname = Name.uniqueKeyGetByName(r.refCols);
                if (t instanceof TList) {
                    sb.append("---@field ");
                    sb.append(String.format("%s table<number,%s>", refname, dsttable)); //refname, islist, dsttable, dstgetname, i));
                    sb.append("\n");
                } else {
                    sb.append("---@field ");
                    sb.append(String.format("%s %s", refname, dsttable)); //refname, islist, dsttable, dstgetname, i));
                    sb.append("\n");
                }
                hasRef = true;
            }
        }
        //没处理外键的相关生成
        //忽略ListRef
        if (hasRef) {
            return sb.toString();
        } else {
            return "";
        }
    }

    private static String typeToLuaType(String type) {
        if (type.equals("int") || type.equals("long") || type.equals("float")) {
            return "number";
        }
        if (type.equals("bool")) {
            return "boolean";
        }
        if (type.startsWith("list")) {//list,int,4
            String[] split = type.split(",");
            return String.format("table<number,%s>", typeToLuaType(split[1]));
        }
        if (type.equals("string") || type.equals("text"))
            return type;
        return "any";
    }

    static String getLuaTextFieldsString(TBean tbean) {
        List<String> texts = new ArrayList<>();
        for (Type col : tbean.getColumns()) {
            if (col.hasText()) {
                if (col instanceof TString) {
                    texts.add(Generator.lower1(col.getColumnName()) + " = true");
                } else if (col instanceof TList && ((TList) col).value instanceof TString) {
                    texts.add(Generator.lower1(col.getColumnName()) + " = 2");
                }
            }
        }

        if (texts.isEmpty()) {
            return "";
        }

        return "\n    { " + String.join(", ", texts) + " },";
    }
}
