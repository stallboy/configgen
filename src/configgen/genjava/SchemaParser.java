package configgen.genjava;

import configgen.define.Bean;
import configgen.define.Table;
import configgen.gen.LangSwitch;
import configgen.type.*;
import configgen.value.AllValue;
import configgen.value.VTable;

import java.util.Map;

public final class SchemaParser {

    public static SchemaInterface parse(AllValue vdb, LangSwitch langSwitch) {
        SchemaInterface root = new SchemaInterface();
        if (langSwitch != null) {
            root.addImp("Text", parseLangSwitch(langSwitch)); //这里国际化的字段当作一个Bean
        }
        for (TBean tBean : vdb.getTDb().getTBeans()) {
            root.addImp(tBean.name, parseBean(tBean));
        }
        for (VTable vTable : vdb.getVTables()) {
            TTable tTable = vTable.getTTable();

            if (tTable.getTableDefine().isEnumFull()) {
                root.addImp(tTable.name, parseEnum(vTable));
                if (!tTable.getTableDefine().isEnumHasOnlyPrimaryKeyAndEnumStr()) {
                    root.addImp(tTable.name + "_Detail", parseBean(tTable.getTBean()));
                }
            } else {
                root.addImp(tTable.name, parseBean(tTable.getTBean()));
                if (tTable.getTableDefine().isEnumPart()) {
                    root.addImp(tTable.name + "_Entry", parseEnum(vTable));
                }
            }
        }
        return root;
    }

    private static Schema parseLangSwitch(LangSwitch ls) {
        SchemaBean sb = new SchemaBean(false);
        for (LangSwitch.Lang lang : ls.getAllLangInfo()) {
            sb.addColumn(lang.getLang(), SchemaPrimitive.SStr);
        }
        return sb;
    }

    private static Schema parseEnum(VTable vTable) {
        boolean isEnumPart = vTable.getTTable().getTableDefine().enumType == Table.EnumType.Entry;
        boolean hasIntValue = !vTable.getTTable().getTableDefine().isEnumAsPrimaryKey();
        SchemaEnum se = new SchemaEnum(isEnumPart, hasIntValue);

        if (hasIntValue) {
            for (Map.Entry<String, Integer> stringIntegerEntry : vTable.getEnumNameToIntegerValueMap().entrySet()) {
                se.addValue(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());
            }
        } else {
            for (String enumName : vTable.getEnumNames()) {
                se.addValue(enumName);
            }
        }
        return se;
    }

    private static Schema parseBean(TBean tBean) {
        if (tBean.getBeanDefine().type == Bean.BeanType.BaseDynamicBean) {
            SchemaInterface si = new SchemaInterface();
            for (TBean subBean : tBean.getChildDynamicBeans()) {
                si.addImp(subBean.name, parseBean(subBean));
            }
            return si;
        } else {
            SchemaBean sb = new SchemaBean(tBean.getBeanDefine().type == Bean.BeanType.Table);
            for (Map.Entry<String, Type> stringTypeEntry : tBean.getColumnMap().entrySet()) {
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
                throw new AssertionError("不该访问到TBean");
            }

            @Override
            public Schema visit(TBeanRef type) {
                return new SchemaRef(type.tBean.name);
            }
        });
    }


}
