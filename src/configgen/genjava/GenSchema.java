package configgen.genjava;

import configgen.define.Bean;
import configgen.define.Table;
import configgen.type.*;
import configgen.value.VDb;
import configgen.value.VTable;

import java.util.Map;

public final class GenSchema {

    public static SchemaInterface parse(VDb vdb) {
        SchemaInterface root = new SchemaInterface();
        for (TBean tBean : vdb.getDbType().tbeans.values()) {
            root.addImp(tBean.name, parseBean(tBean));
        }
        for (VTable vTable : vdb.getVTables()) {
            TTable tTable = vTable.tableType;

            if (tTable.tableDefine.isEnumFull()) {
                root.addImp(tTable.name, parseEnum(vTable));
                if (!tTable.tableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                    root.addImp(tTable.name + "_Detail", parseBean(tTable.tbean));
                }
            } else {
                root.addImp(tTable.name, parseBean(tTable.tbean));
                if (tTable.tableDefine.isEnumPart()) {
                    root.addImp(tTable.name + "_Entry", parseEnum(vTable));
                }
            }
        }
        return root;
    }

    private static Schema parseEnum(VTable vTable) {
        boolean isEnumPart = vTable.tableType.tableDefine.enumType == Table.EnumType.EnumPart;
        boolean hasIntValue = !vTable.tableType.tableDefine.isEnumAsPrimaryKey();
        SchemaEnum se = new SchemaEnum(isEnumPart, hasIntValue);

        if (hasIntValue) {
            for (Map.Entry<String, Integer> stringIntegerEntry : vTable.enumName2IntegerValueMap.entrySet()) {
                se.addValue(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());
            }
        } else {
            for (String enumName : vTable.enumNames) {
                se.addValue(enumName);
            }
        }
        return se;
    }

    private static Schema parseBean(TBean tBean) {
        if (tBean.beanDefine.type == Bean.BeanType.BaseDynamicBean) {
            SchemaInterface si = new SchemaInterface();
            for (TBean subBean : tBean.childDynamicBeans.values()) {
                si.addImp(subBean.name, parseBean(subBean));
            }
            return si;
        } else {
            SchemaBean sb = new SchemaBean(tBean.beanDefine.type == Bean.BeanType.Table);
            for (Map.Entry<String, Type> stringTypeEntry : tBean.columns.entrySet()) {
                sb.addColumn(stringTypeEntry.getKey(), parseType(stringTypeEntry.getValue()));
            }
            return sb;
        }
    }

    private static Schema parseType(Type t) {
        return t.accept(new TypeVisitorT<Schema>() {
            @Override
            public Schema visit(TBool type) {
                return SchemaPrimitive.SBool;
            }

            @Override
            public Schema visit(TInt type) {
                return SchemaPrimitive.SInt;
            }

            @Override
            public Schema visit(TLong type) {
                return SchemaPrimitive.SLong;
            }

            @Override
            public Schema visit(TFloat type) {
                return SchemaPrimitive.SFloat;
            }

            @Override
            public Schema visit(TString type) {
                return SchemaPrimitive.SStr;
            }

            @Override
            public Schema visit(TList type) {
                return new SchemaList(parseType(type.value));
            }

            @Override
            public Schema visit(TMap type) {
                return new SchemaMap(parseType(type.key), parseType(type.value));
            }

            @Override
            public Schema visit(TBean type) {
                return new SchemaRef(type.name);
            }
        });
    }


}
