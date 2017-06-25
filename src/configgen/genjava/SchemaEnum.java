package configgen.genjava;

import java.util.ArrayList;
import java.util.List;

public class SchemaEnum implements Schema {
    public static class EnumValue {
        public String name;
        public int intValue;
        public String strValue;

        public boolean compatible(EnumValue other, boolean isStrValue) {
            if (!name.equals(other.name)) {
                return false;
            }
            if (isStrValue) {
                return strValue.equals(other.strValue);
            } else {
                return intValue == other.intValue;
            }
        }
    }

    public boolean isStrValue;
    public List<EnumValue> values = new ArrayList<>();

    @Override
    public boolean compatible(Schema other) {
        if (other == null || !(other instanceof SchemaEnum)) {
            return false;
        }
        SchemaEnum se = (SchemaEnum) other;
        if (isStrValue != se.isStrValue) {
            return false;
        }
        if (values.size() != se.values.size()) {
            return false;
        }

        for (int i = 0; i < values.size(); i++) {
            EnumValue v1 = values.get(i);
            EnumValue v2 = se.values.get(i);
            if (!v1.compatible(v2, isStrValue)) {
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
        output.writeBool(isStrValue);
        output.writeInt(values.size());
        for (EnumValue value : values) {
            output.writeStr(value.name);
            if (isStrValue) {
                output.writeStr(value.strValue);
            } else {
                output.writeInt(value.intValue);
            }
        }
    }

    public void read(ConfigInput input) {
        isStrValue = input.readBool();
        values = new ArrayList<>();
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            EnumValue ev = new EnumValue();
            ev.name = input.readStr();
            if (isStrValue) {
                ev.strValue = input.readStr();
            } else {
                ev.intValue = input.readInt();
            }
            values.add(ev);
        }
    }

}
