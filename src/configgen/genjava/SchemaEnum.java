package configgen.genjava;

import java.util.ArrayList;
import java.util.List;

//为了服务器热更新配置，也是拼了，会把枚举的key值生成到代码里，然后根据key动态去另一个表中取其他具体的值
//还要考虑全枚举，在java中switch 自动生成case
// 所以这里要保证枚举的具体的key值完全一样
public class SchemaEnum implements Schema {
    public static class EnumValue {
        public String name;
        public int intValue;

        public boolean compatible(EnumValue other, boolean hasIntValue) {
            if (!name.equals(other.name)) {
                return false;
            }
            if (hasIntValue) {
                return intValue == other.intValue;
            }else{
                return true;
            }
        }
    }

    public boolean hasIntValue;
    public List<EnumValue> values = new ArrayList<>();

    @Override
    public boolean compatible(Schema other) {
        if (other == null || !(other instanceof SchemaEnum)) {
            return false;
        }
        SchemaEnum se = (SchemaEnum) other;
        if (hasIntValue != se.hasIntValue) {
            return false;
        }
        if (values.size() != se.values.size()) {
            return false;
        }

        for (int i = 0; i < values.size(); i++) {
            EnumValue v1 = values.get(i);
            EnumValue v2 = se.values.get(i);
            if (!v1.compatible(v2, hasIntValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void write(ConfigOutput output) {
        output.writeInt(ENUM);
        output.writeBool(hasIntValue);
        output.writeInt(values.size());
        for (EnumValue value : values) {
            output.writeStr(value.name);

            if (hasIntValue){
                output.writeInt(value.intValue);
            }
        }
    }

    public void read(ConfigInput input) {
        hasIntValue = input.readBool();
        values = new ArrayList<>();
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            EnumValue ev = new EnumValue();
            ev.name = input.readStr();
            if (hasIntValue) {
                ev.intValue = input.readInt();
            }
            values.add(ev);
        }
    }

}
