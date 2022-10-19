package configgen.value;

import configgen.define.Range;
import configgen.type.TString;

import java.util.List;

public class VString extends VPrimitive {
    /**
     * 生成代码用这个
     * 如果是i18n，即要国际化，那构造时会直接提取设置此value，这样对Generator隐藏掉这个机制
     */
    public final String value;

    VString(TString type, List<Cell> data) {
        super(type, data);
        var ctx = AllValue.getCurrent().getCtx();
        String sValue;
        if (type.subtype == TString.Subtype.STRING) {
            sValue = raw.getData();
        } else {
            String originalValue = raw.getData();
            String v = AllValue.getCurrent().getCtx().getI18n().enterText(originalValue);
            sValue = v != null ? v : originalValue;
            if (AllValue.getCurrent().getCtx().isI18n() && v == null && !originalValue.isEmpty()) {
                System.out.println("未找到 " + type.fullName() + ": " + originalValue);
            }
        }
        var replacement = ctx.getReplacement();
        value = replacement == null ? sValue : replacement.replace(sValue);
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
