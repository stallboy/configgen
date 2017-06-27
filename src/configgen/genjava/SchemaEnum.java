package configgen.genjava;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//为了服务器热更新配置，也是拼了，
//这里会把枚举的key值生成到代码里，然后根据key动态去另一个表中取其他具体的值
//考虑全枚举，在java中switch 自动生成case，这里要保证全枚举的具体的key值完全一样
//针对半枚举，要保证代码里的枚举，在数据里都在，并且值一样。

public class SchemaEnum implements Schema {
    public final boolean isEnumPart;
    public final boolean hasIntValue;
    public final Map<String, Integer> values = new LinkedHashMap<>();

    public SchemaEnum(ConfigInput input) {
        isEnumPart = input.readBool();
        hasIntValue = input.readBool();
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            if (hasIntValue) {
                values.put(input.readStr(), input.readInt());
            } else {
                values.put(input.readStr(), 0);
            }
        }
    }

    public SchemaEnum(boolean isEnumPart, boolean hasIntValue) {
        this.isEnumPart = isEnumPart;
        this.hasIntValue = hasIntValue;
    }


    public void addValue(String name, int intValue) {
        values.put(name, intValue);
    }

    public void addValue(String name) {
        values.put(name, 0);
    }

    @Override
    public boolean compatible(Schema other) {
        if (other == null || !(other instanceof SchemaEnum)) {
            return false;
        }
        SchemaEnum newData = (SchemaEnum) other;
        if (isEnumPart != newData.isEnumPart) {
            return false;
        }
        if (hasIntValue != newData.hasIntValue) {
            return false;
        }

        if (!isEnumPart) {
            if (values.size() != newData.values.size()) {
                return false;
            }
        }

        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            String codeName = entry.getKey();
            int codeV = entry.getValue();
            Integer newDataValue = newData.values.get(codeName);
            if (newDataValue == null) {
                return false;
            }

            if (hasIntValue) {
                if (codeV != newDataValue) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(VisitorT<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void write(ConfigOutput output) {
        output.writeInt(ENUM);
        output.writeBool(isEnumPart);
        output.writeBool(hasIntValue);
        output.writeInt(values.size());
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            output.writeStr(entry.getKey());
            if (hasIntValue) {
                output.writeInt(entry.getValue());
            }
        }
    }

}
