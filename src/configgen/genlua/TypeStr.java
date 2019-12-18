package configgen.genlua;

import configgen.define.Column;
import configgen.gen.Generator;
import configgen.type.*;
import configgen.value.VBool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class TypeStr {

    private static boolean packBool = false;

    static void setPackBool(boolean pack) {
        packBool = pack;
    }

    // uniqkeys : {{allname=, getname=, keyidx1=, keyidx2=}, }
    static String getLuaUniqKeysString(TTable ttable) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        sb.append(getLuaOneUniqKeyString(ttable, ttable.getPrimaryKey(), true));
        for (Map<String, Type> uniqueKey : ttable.getUniqueKeys()) {
            sb.append(getLuaOneUniqKeyString(ttable, uniqueKey, false));
        }
        sb.append("}");
        return sb.toString();
    }

    private static String getLuaOneUniqKeyString(TTable ttable, Map<String, Type> keys, boolean isPrimaryKey) {
        String allname = isPrimaryKey ? "all" : Name.uniqueKeyMapName(keys);
        String getname = isPrimaryKey ? "get" : Name.uniqueKeyGetByName(keys);

        Iterator<Type> it = keys.values().iterator();
        Type key1 = it.next();
        int keyidx1 = key1.getColumnIndex() + 1;

        boolean hasKeyIdx2 = false;
        int keyidx2 = 0;
        if (keys.size() > 1) {
            if (keys.size() != 2) {
                throw new RuntimeException("uniqkeys size != 2 " + ttable.name);
            }
            Type key2 = it.next();
            hasKeyIdx2 = true;
            keyidx2 = key2.getColumnIndex() + 1;
        }

        if (hasKeyIdx2) {
            return String.format("{ \"%s\", \"%s\", %d, %d }, ", allname, getname, keyidx1, keyidx2);
        } else {
            return String.format("{ \"%s\", \"%s\", %d }, ", allname, getname, keyidx1);
        }
    }

    static String getLuaEnumIdxString(TTable ttable) {
        Type enumCol = ttable.getEnumColumnType();
        if (enumCol == null) {
            return "nil";
        } else {
            return String.valueOf(enumCol.getColumnIndex() + 1);
        }
    }

    // refs { {refname, islist, dsttable, dstgetname, keyidx1, keyidx2}, }
    static String getLuaRefsString(TBean tbean) {
        StringBuilder sb = new StringBuilder();
        boolean hasRef = false;
        sb.append("{ ");
        int i = 0;
        for (Type t : tbean.getColumns()) {
            i++;
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
                sb.append(String.format("\n    { \"%s\", %s, %s, \"%s\", %d }, ", refname, islist, dsttable, dstgetname, i));
                hasRef = true;
            }
        }

        for (TForeignKey mRef : tbean.getMRefs()) {
            String refname = Name.refName(mRef);
            String dsttable = Name.fullName(mRef.refTable);
            String dstgetname = Name.uniqueKeyGetByName(mRef.foreignKeyDefine.ref.cols);

            int keyidx1 = mRef.thisTableKeys[0].getColumnIndex() + 1;

            boolean hasKeyIdx2 = false;
            int keyidx2 = 0;
            if (mRef.thisTableKeys.length > 1) {
                if (mRef.thisTableKeys.length != 2) {
                    throw new RuntimeException("keys length != 2 " + tbean.name);
                }
                hasKeyIdx2 = true;
                keyidx2 = mRef.thisTableKeys[1].getColumnIndex() + 1;
            }
            if (hasKeyIdx2) {
                sb.append(String.format("\n    { \"%s\", false, %s, \"%s\", %d, %d }, ", refname, dsttable, dstgetname, keyidx1, keyidx2));
            } else {
                sb.append(String.format("\n    { \"%s\", false, %s, \"%s\", %d }, ", refname, dsttable, dstgetname, keyidx1));
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

    static String getLuaFieldsString(TBean tbean) {
        StringBuilder sb = new StringBuilder();

        int cnt = tbean.getColumnMap().size();
        int i = 0;

        boolean doPack = packBool;
        if (packBool) {
            int boolCnt = tbean.getBoolFieldCount();
            if (boolCnt >= 50) {
                throw new RuntimeException("现在不支持pack多余50个bool字段的bean");
            }

            if (boolCnt < 2) {
                doPack = false;
            }
        }
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
                            sb.append("\n    \"").append(Generator.lower1(bn)).append("\", -- ").append(f.type).append(c);
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
                Column f = tbean.getBeanDefine().columns.get(n);
                String c = f.desc.isEmpty() ? "" : ", " + f.desc;
                if (i < cnt) {
                    sb.append("\n    \"").append(Generator.lower1(n)).append("\", -- ").append(f.type).append(c);
                } else {
                    sb.append("\n    \"").append(Generator.lower1(n)).append("\"  -- ").append(f.type).append(c);
                }
            }

        }

        return sb.toString();
    }

    static String getLuaFieldsStringEmmyLua(TBean tbean) {
        StringBuilder sb = new StringBuilder();
        int cnt = tbean.getColumnMap().size();
        int i = 0;
        for (String n : tbean.getColumnMap().keySet()) {
            i++;
            Column f = tbean.getBeanDefine().columns.get(n);
            String c = f.desc.isEmpty() ? "" : ", " + f.desc;
            sb.append("---@field ").append(Generator.lower1(n)).append(" ").append(typeToLuaType(f.type)).append(" ").append(c).append("\n");
        }
        return sb.toString();
    }

    static String getLuaRefsStringEmmyLua(TBean tbean) {
        StringBuilder sb = new StringBuilder();
        boolean hasRef = false;
        int i = 0;
        for (Type t : tbean.getColumns()) {
            i++;
            for (SRef r : t.getConstraint().references) {
                if (t instanceof TMap) {
                    System.out.println("map sref not suppport, bean=" + tbean.name);
                    break;
                }
                String refname = Name.refName(r);
                String dsttable = Name.fullName(r.refTable);
                String dstgetname = Name.uniqueKeyGetByName(r.refCols);
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
