package configgen.value;

import configgen.Node;
import configgen.type.*;

import java.util.List;

public class Value extends Node {

    public Value(Node parent, String link) {
        super(parent, link);
    }

    public static Value create(Node parent, String link, Type t, List<Cell> data) {
        return t.accept(new TVisitor<Value>() {
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
