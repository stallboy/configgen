package configgen.genlua;

import configgen.value.*;

import java.util.*;

public class ValueSharedLayer implements ValueVisitor {
    static class VCompositeCnt {
        int cnt;
        VComposite first;
        private boolean traversed = false;

        VCompositeCnt(VComposite first) {
            this.first = first;
            cnt = 1;
        }

        int getCnt() {
            return cnt;
        }

        VComposite getFirst() {
            return first;
        }

        boolean isTraversed() {
            return traversed;
        }

        void setTraversed() {
            this.traversed = true;
        }
    }

    private final ValueShared shared;
    private final Map<VComposite, VCompositeCnt> compositeValueToCnt;
    private final boolean isCurr;
    private final ValueSharedLayer next;

    ValueSharedLayer(ValueShared shared) {
        this.shared = shared;
        compositeValueToCnt = new LinkedHashMap<>();
        isCurr = true;
        next = new ValueSharedLayer(this);
    }

    private ValueSharedLayer(ValueSharedLayer last) {
        shared = last.shared;
        compositeValueToCnt = last.compositeValueToCnt;
        isCurr = false;
        next = null;
    }

    Map<VComposite, VCompositeCnt> getCompositeValueToCnt() {
        return compositeValueToCnt;
    }

    private void add(VComposite v) {
        VCompositeCnt oldInThisLayer = compositeValueToCnt.get(v);
        if (oldInThisLayer != null) {
            oldInThisLayer.cnt++;
            oldInThisLayer.first.setShared(); //设置上，后面生成代码时会快点
            v.setShared();
        } else {
            VCompositeCnt oldInPreviousLayer = shared.remove(v);
            if (oldInPreviousLayer != null) { //前面的层可能包含了这个v
                oldInPreviousLayer.cnt++;
                compositeValueToCnt.put(v, oldInPreviousLayer); //挪到这层，这样生成lua代码时已经排序,但要在生成下层shared时不遍历这个，因为已经遍历过
                oldInPreviousLayer.first.setShared();
                v.setShared();

            } else {
                compositeValueToCnt.put(v, new VCompositeCnt(v));
            }

        }
    }

    @Override
    public void visit(VBool value) {

    }

    @Override
    public void visit(VInt value) {

    }

    @Override
    public void visit(VLong value) {

    }

    @Override
    public void visit(VFloat value) {

    }

    @Override
    public void visit(VString value) {

    }

    @Override
    public void visit(VList value) {
        if (isCurr) {
            for (Value ele : value.getList()) {
                ele.accept(next);
            }
        } else if (!value.getList().isEmpty()) {
            add(value);
        }
    }

    @Override
    public void visit(VMap value) {
        if (isCurr) {
            for (Map.Entry<Value, Value> entry : value.getMap().entrySet()) {
                entry.getKey().accept(next);
                entry.getValue().accept(next);
            }
        } else if (!value.getMap().isEmpty()) {
            add(value);
        }
    }

    @Override
    public void visit(VBean value) {
        if (isCurr) {

            for (Value field : value.getValues()) {
                field.accept(next);
            }
        } else if (!value.getValues().isEmpty()) {
            add(value);
        }
    }
}
