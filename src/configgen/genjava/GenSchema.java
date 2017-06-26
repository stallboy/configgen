package configgen.genjava;

import configgen.define.Bean;
import configgen.type.*;
import configgen.value.VDb;
import configgen.value.VTable;

import java.util.Map;

public class GenSchema {

    public static SchemaInterface gen(VDb vdb) {
        SchemaInterface root = new SchemaInterface();
        for (TBean tBean : vdb.dbType.tbeans.values()) {
            root.implementations.put(tBean.name, parseBean(tBean));
        }
        for (VTable vTable : vdb.vtables.values()) {
            TTable tTable = vTable.tableType;

            if (tTable.tableDefine.isEnum()) {
                if (tTable.tableDefine.isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                    root.implementations.put(tTable.name, parseEnum(vTable));
                } else {
                    root.implementations.put(tTable.name + "Enum", parseEnum(vTable));
                    root.implementations.put(tTable.name, parseBean(tTable.tbean));
                }
            } else {
                root.implementations.put(tTable.name, parseBean(tTable.tbean));
            }
        }
        return root;
    }

    private static Schema parseEnum(VTable vTable) {
        SchemaEnum se = new SchemaEnum();
        se.hasIntValue = !vTable.tableType.tableDefine.isEnumAsPrimaryKey();
        if (se.hasIntValue){

        }else{
            for (String enumName : vTable.enumNames) {
                SchemaEnum.EnumValue ev = new SchemaEnum.EnumValue();
                ev.name = enumName;
                se.values.add(ev);
            }
        }
        return se;
    }

    private static Schema parseBean(TBean tBean) {
        if (tBean.beanDefine.type == Bean.BeanType.BaseAction) {
            SchemaInterface si = new SchemaInterface();
            for (TBean subBean : tBean.actionBeans.values()) {
                si.implementations.put(subBean.name, parseBean(subBean));
            }
            return si;
        } else {
            SchemaBean sb = new SchemaBean();
            sb.isTable = tBean.beanDefine.type == Bean.BeanType.Table;
            for (Map.Entry<String, Type> stringTypeEntry : tBean.columns.entrySet()) {
                SchemaBean.Column col = new SchemaBean.Column();
                col.name = stringTypeEntry.getKey();
                col.schema = parseType(stringTypeEntry.getValue());
                sb.columns.add(col);
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
                SchemaList sl = new SchemaList();
                sl.ele = parseType(type.value);
                return sl;
            }

            @Override
            public Schema visit(TMap type) {
                SchemaMap sm = new SchemaMap();
                sm.key = parseType(type.key);
                sm.value = parseType(type.value);
                return sm;
            }

            @Override
            public Schema visit(TBean type) {
                SchemaRef sr = new SchemaRef();
                sr.type = type.name;
                return sr;
            }
        });
    }


}
