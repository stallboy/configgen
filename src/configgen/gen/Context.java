package configgen.gen;

import configgen.data.DDb;
import configgen.define.Db;
import configgen.type.TDb;
import configgen.value.I18n;
import configgen.value.VDb;

import java.util.Objects;

public class Context {
    public final Db define;
    public final TDb type;
    public final DDb data;
    public final I18n i18n;

    Context(Db define, TDb type, DDb data, I18n i18n) {
        this.define = define;
        this.type = type;
        this.data = data;
        this.i18n = i18n;
    }

    private VDb lastValue;
    private String lastValueOwn;

    public void verify(){
        VDb value = new VDb(type, data, i18n);
        value.verifyConstraint();
    }

    public VDb makeValue() {
        return makeValue(null);
    }

    public VDb makeValue(String own) {
        if (lastValue != null) {
            if (Objects.equals(own, lastValueOwn)) {
                return lastValue;
            }
            lastValue = null;
        }

        VDb value;
        if (own == null || own.isEmpty()) {
            value = new VDb(type, data, i18n);
            value.verifyConstraint();
        } else {
            Db ownDefine = define.extract(own);
            TDb ownType = new TDb(ownDefine);
            ownType.resolve();

            value = new VDb(ownType, data, i18n);
            value.verifyConstraint();
        }

        lastValueOwn = own;
        lastValue = value;
        return value;
    }

}
