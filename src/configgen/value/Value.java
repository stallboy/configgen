package configgen.value;

import configgen.Node;
import configgen.type.*;

import java.util.List;

public abstract class Value extends Node {
    protected Type type;
    protected List<Cell> cells;

    public Value(Node parent, String link, Type type, List<Cell> data) {
        super(parent, link);
        this.type = type;
        this.cells = data;
    }

    public abstract void accept(ValueVisitor visitor);

    public abstract void verifyConstraint();

    protected void verifyRefs() {
        for (SRef ref : type.constraint.refs) {
            if (ref.ref != null){
                if (isNull()){
                    Assert(ref.nullable, toString(), "null not support ref", ref.ref.location());
                }else{
                    Assert(ref.ref.value.vkeys.contains(this), toString(), "not found in ref", ref.ref.location());
                }
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
        sb.append(cells.get(0).toString());
        for (int i = 1; i < cells.size(); i++) {
            sb.append(",").append(cells.get(i).data);
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
            public Value visit(TText type) {
                return new VText(parent, link, type, data);
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
