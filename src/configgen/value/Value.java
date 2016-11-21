package configgen.value;

import configgen.Node;
import configgen.type.*;

import java.util.List;
import java.util.Set;

public abstract class Value extends Node {
    public final Type type;
    protected final List<Cell> cells;

    public Value(Node parent, String name, Type type, List<Cell> data) {
        super(parent, name);
        this.type = type;
        this.cells = data;
    }

    public abstract void accept(ValueVisitor visitor);

    public abstract void verifyConstraint();

    protected void verifyRefs() {
        for (SRef sref : type.constraint.references) {
            if (isNull()) {
                require(sref.refNullable, toString(), "null not support ref", sref.refTable.fullName());
            } else {
                require(sref.refTable != null, "");
                VTable vtable = ((VDb) root).vtables.get(sref.refTable.name);
                Set<Value> keyValueSet = sref.refToPrimaryKey() ? vtable.primaryKeyValueSet : vtable.uniqueKeyValueSetMap.get(String.join(",", sref.refCols));
                require(keyValueSet.contains(this), toString(), "not found in ref", sref.refTable.fullName());
            }
        }
    }

    public boolean isNull() {
        for (Cell cell : cells) {
            if (!cell.data.trim().isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!cells.isEmpty()){
            sb.append(cells.get(0).toString());
            for (int i = 1; i < cells.size(); i++) {
                sb.append(",").append(cells.get(i).data);
            }
        }
        return sb.toString();
    }


    public static Value create(Node parent, String link, Type t, List<Cell> data) {
        return t.accept(new TypeVisitorT<Value>() {
            @Override
            public Value visit(TBool type) {
                return new VBool(parent, link, type, data);
            }

            @Override
            public Value visit(TInt type) {
                return new VInt(parent, link, type, data);
            }

            @Override
            public Value visit(TLong type) {
                return new VLong(parent, link, type, data);
            }

            @Override
            public Value visit(TFloat type) {
                return new VFloat(parent, link, type, data);
            }

            @Override
            public Value visit(TString type) {
                return new VString(parent, link, type, data);
            }

            @Override
            public Value visit(TList type) {
                return new VList(parent, link, type, data);
            }

            @Override
            public Value visit(TMap type) {
                return new VMap(parent, link, type, data);
            }

            @Override
            public Value visit(TBean type) {
                return new VBean(parent, link, type, data);
            }
        });
    }
}
