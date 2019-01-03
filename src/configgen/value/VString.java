package configgen.value;

import configgen.Logger;
import configgen.define.Range;
import configgen.type.TString;

import java.util.List;

public class VString extends VPrimitive {
    public final TString tstring;
    public final String value; //生成代码用这个


    VString(TString type, List<Cell> data) {
        super(type, data);
        tstring = type;

        if (tstring.subtype == TString.Subtype.STRING) {
            value = raw.data;
        } else {
            String originalValue = raw.data;
            String v = VDb.getCurrent().getI18n().enterText(originalValue);
            value = v != null ? v : originalValue;
        }
    }

    @Override
    public boolean checkRange(Range range) {
        int len = value.length();
        return len >= range.min && len <= range.max;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof VString && value.equals(((VString) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public void accept(ValueVisitor visitor) {
        visitor.visit(this);
    }
}
