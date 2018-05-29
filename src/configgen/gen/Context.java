package configgen.gen;

import configgen.data.DDb;
import configgen.define.Db;
import configgen.type.TDb;
import configgen.value.VDb;

import java.util.Objects;

public class Context {
    public final Db define;
    public final TDb type;
    public final DDb data;

    Context(Db define, TDb type, DDb data) {
        this.define = define;
        this.type = type;
        this.data = data;
    }

    private VDb lastValue;
    private String lastValueOwn;

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
            value = new VDb(type, data);
            value.verifyConstraint();
        } else {
            Db ownDefine = define.extract(own);
            TDb ownType = new TDb(ownDefine);
            ownType.resolve();

            value = new VDb(ownType, data);
            value.verifyConstraint();
        }

        lastValueOwn = own;
        lastValue = value;
        return value;
    }

}
