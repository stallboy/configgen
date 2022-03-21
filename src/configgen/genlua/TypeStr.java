package configgen.genlua;

import configgen.define.Column;
import configgen.gen.Generator;
import configgen.type.*;
import configgen.value.VTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class TypeStr {


    // {{allName, getName=, keyIdx1=, keyIdx2=}, }
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
        String allname = isPrimaryKey ? Name.primaryKeyMapName : Name.uniqueKeyMapName(keys);
        String getname = isPrimaryKey ? Name.primaryKeyGetName : Name.uniqueKeyGetByName(keys);

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
        boolean doPack = AContext.getInstance().isPackBool();
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


    /**
     * {refName, 0, dstTable, dstGetName, thisColumnIdx, [thisColumnIdx2]}, -- 最常见类型
     * {refName, 1, dstTable, dstGetName, thisColumnIdx}, --本身是list
     * {refName, 2, dstTable, dstAllName, thisColumnIdx, dstColumnIdx}, --listRef到别的表
     * {refName, 3, dstTable, dstGetName, thisColumnIdx}, --本身是map
     */
    static String getLuaRefsString(TBean tbean, boolean isUseColumnStore) {
        StringBuilder sb = new StringBuilder();
        boolean hasRef = false;
        sb.append("{ ");

        for (Type t : tbean.getColumns()) {
            for (SRef r : t.getConstraint().references) {
                if (r.refTable == null) { //只在map key做ref时存在，lua生成忽略这种情况
                    continue;
                }
                String refName = Name.refName(r);
                String dstTable = Name.fullName(r.refTable);
                String dstGetName = Name.uniqueKeyGetByName(r.refCols);
                String thisColumnIdx = getColumnStrOrIndex(t, tbean, isUseColumnStore);

                if (t instanceof TList) {
                    // {refName, 1, dstTable, dstGetName, thisColumnIdx}, --本身是list
                    sb.append(String.format("\n    { '%s', 1, %s, '%s', %s }, ", refName, dstTable, dstGetName, thisColumnIdx));
                } else if (t instanceof TMap) {
                    // {refName, 3, dstTable, dstGetName, thisColumnIdx}, --本身是map
                    sb.append(String.format("\n    { '%s', 3, %s, '%s', %s }, ", refName, dstTable, dstGetName, thisColumnIdx));

                } else {
                    // {refName, 0, dstTable, dstGetName, thisColumnIdx}  --最常见类型
                    sb.append(String.format("\n    { '%s', 0, %s, '%s', %s }, ", refName, dstTable, dstGetName, thisColumnIdx));
                }
                hasRef = true;
            }
        }

        for (TForeignKey mRef : tbean.getMRefs()) {
            // {refName, 0, dstTable, dstGetName, thisColumnIdx, thisColumnIdx2}, -- 最常见类型
            String refName = Name.refName(mRef);
            String dstTable = Name.fullName(mRef.refTable);
            String dstGetName = Name.uniqueKeyGetByName(mRef.foreignKeyDefine.ref.cols);
            if (mRef.thisTableKeys.length != 2) {
                throw new RuntimeException("lua只支持对多两列索引！，keys length != 2 " + tbean.name);
            }

            String thisColumnIdx = getColumnStrOrIndex(mRef.thisTableKeys[0], tbean, isUseColumnStore);
            String thisColumnIdx2 = getColumnStrOrIndex(mRef.thisTableKeys[1], tbean, isUseColumnStore);

            sb.append(String.format("\n    { '%s', 0, %s, '%s', %s, %s }, ",
                                    refName, dstTable, dstGetName, thisColumnIdx, thisColumnIdx2));

            hasRef = true;
        }

        for (TForeignKey listRef : tbean.getListRefs()) {
            //{refName, 2, dstTable, dstAllName, thisColumnIdx, dstColumnIdx}, --listRef到别的表
            String refName = Name.refName(listRef);
            String dstTable = Name.fullName(listRef.refTable);
            String dstAllName = Name.primaryKeyMapName;

            String thisColumnIdx = getColumnStrOrIndex(listRef.thisTableKeys[0], tbean, isUseColumnStore);
            String dstColumnIdx = getColumnStrOrIndex(listRef.getRefTypeKeys()[0], listRef.refTable.getTBean(), isUseColumnStore);

            sb.append(String.format("\n    { '%s', 2, %s, '%s', %s, %s }, ",
                                    refName, dstTable, dstAllName, thisColumnIdx, dstColumnIdx));

            hasRef = true;
        }


        sb.append("}");

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
        boolean has = false;
        for (Map.Entry<String, Type> entry : tbean.getColumnMap().entrySet()) {
            String name = entry.getKey();
            Column f = tbean.getBeanDefine().columns.get(name);
            String c = f.desc.isEmpty() ? "" : ", " + f.desc;
            Type type = entry.getValue();
            sb.append("---@field ").append(Generator.lower1(name)).append(" ").append(typeToLuaType(type)).append(" ").append(c).append("\n");
            has = true;
        }
        if (has) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    static String getLuaUniqKeysStringEmmyLua(TTable ttable) {
        StringBuilder sb = new StringBuilder();
        String fullName = Name.fullName(ttable);
        sb.append(String.format("---@field %s fun(%s):%s\n",
                                Name.primaryKeyGetName, getLuaGetParam(ttable.getPrimaryKey()), fullName));
        for (Map<String, Type> uniqueKey : ttable.getUniqueKeys()) {
            sb.append(String.format("---@field %s fun(%s):%s\n",
                                    Name.uniqueKeyGetByName(uniqueKey), getLuaGetParam(uniqueKey), fullName));
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static String getLuaGetParam(Map<String, Type> primaryOrUniqueKey) {
        StringBuilder sb = new StringBuilder();
        boolean has = false;
        for (Map.Entry<String, Type> entry : primaryOrUniqueKey.entrySet()) {
            sb.append(entry.getKey()).append(":").append(typeToLuaType(entry.getValue())).append(",");
            has = true;
        }
        if (has) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }


    static String getLuaEnumStringEmmyLua(VTable vtable) {
        StringBuilder sb = new StringBuilder();
        boolean has = false;
        for (String enumName : vtable.getEnumNames()) {
            sb.append("---@field ").append(enumName).append(" ").append(Name.fullName(vtable.getTTable())).append("\n");
            has = true;
        }
        if (has) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    static String getLuaRefsStringEmmyLua(TBean tbean) {
        StringBuilder sb = new StringBuilder();
        boolean hasRef = false;

        for (Type t : tbean.getColumns()) {
            for (SRef r : t.getConstraint().references) {
                if (t instanceof TMap) {
                    break;
                }
                String refName = Name.refName(r);
                String dstTable = Name.fullName(r.refTable);
                if (t instanceof TList) {
                    sb.append(String.format("---@field %s table<number,%s>\n", refName, dstTable));
                } else {
                    sb.append(String.format("---@field %s %s\n", refName, dstTable));
                }
                hasRef = true;
            }
        }

        for (TForeignKey mRef : tbean.getMRefs()) {
            // {refName, 0, dstTable, dstGetName, thisColumnIdx, thisColumnIdx2}, -- 最常见类型
            String refName = Name.refName(mRef);
            String dstTable = Name.fullName(mRef.refTable);
            sb.append(String.format("---@field %s %s\n", refName, dstTable));
            hasRef = true;
        }

        for (TForeignKey listRef : tbean.getListRefs()) {
            //{refName, 2, dstTable, dstAllName, thisColumnIdx, dstColumnIdx}, --listRef到别的表
            String refName = Name.refName(listRef);
            String dstTable = Name.fullName(listRef.refTable);
            sb.append(String.format("---@field %s table<number,%s>\n", refName, dstTable));
            hasRef = true;
        }

        if (hasRef) {
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        } else {
            return "";
        }
    }

    private static String typeToLuaType(Type type) {
        return type.accept(new TypeVisitorT<String>() {
            @Override
            public String visit(TBool type) {
                return "boolean";
            }

            @Override
            public String visit(TInt type) {
                return "number";
            }

            @Override
            public String visit(TLong type) {
                return "number";
            }

            @Override
            public String visit(TFloat type) {
                return "number";
            }

            @Override
            public String visit(TString type) {
                return type.toString();
            }

            @Override
            public String visit(TList type) {
                return String.format("table<number,%s>", typeToLuaType(type.value));
            }

            @Override
            public String visit(TMap type) {
                return String.format("table<%s,%s>", typeToLuaType(type.key), typeToLuaType(type.value));
            }

            @Override
            public String visit(TBean type) {
                return Name.fullName(type);
            }

            @Override
            public String visit(TBeanRef type) {
                return typeToLuaType(type.tBean);
            }
        });

    }

    static String getLuaTextFieldsString(TBean tbean) {
        List<String> texts = new ArrayList<>();
        for (Type col : tbean.getColumns()) {
            if (col.hasText()) {
                if (col instanceof TString) {
                    texts.add(Generator.lower1(col.getColumnName()) + " = 1");
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
