package configgen.value;

import configgen.define.Range;
import configgen.type.TString;

import java.util.List;

public class VString extends VPrimitive {
    public final String value; //生成代码用这个

    VString(TString type, List<Cell> data) {
        super(type, data);

        if (type.subtype == TString.Subtype.STRING) {
            value = raw.data;
        } else {
            String originalValue = raw.data;
            String v = VDb.getCurrent().getCtx().getI18n().enterText(originalValue);
            value = v != null ? v : originalValue;
            if ( VDb.getCurrent().getCtx().isI18n() && v == null && !originalValue.isEmpty() ){
                System.out.println("未找到 " + type.fullName() + ": " + originalValue);
            }
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
