package configgen.value;

import configgen.Node;
import configgen.define.Range;
import configgen.type.TString;

import java.util.List;

public class VString extends VPrimitive {
    public final TString tstring;
    public final String value; //生成代码用这个

    public final String originalValue; //这个原始值
    public final String i18nValue;      //是否有翻译值

    public VString(Node parent, String name, TString type, List<Cell> data) {
        super(parent, name, type, data);
        tstring = type;

        if (tstring.subtype == TString.Subtype.STRING){
            value = raw.data;
            originalValue = value;
            i18nValue = "";
        }else{
            originalValue = raw.data;
            I18n i18n = ((VDb)root).i18n;
            String v = i18n.get(originalValue);
            if (v == null){
                value = originalValue;
                i18nValue = "";
            }else{
                value = v;
                i18nValue = v;
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
        return o != null && o instanceof VString && value.equals(((VString) o).value);
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
