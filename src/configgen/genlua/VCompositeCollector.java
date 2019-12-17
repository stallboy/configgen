package configgen.genlua;

import configgen.value.*;

import java.util.*;

public class VCompositeCollector implements ValueVisitor {
    static public class VCompositeCnt {
        int cnt;
        VComposite first;

        public VCompositeCnt(VComposite first) {
            this.first = first;
            cnt = 1;
        }

        public int getCnt() {
            return cnt;
        }

        public VComposite getFirst() {
            return first;
        }
    }

    private final Map<VComposite, VCompositeCnt> compositeValueToCnt;
    private boolean collectThis;
    private VCompositeCollector collector;

    public VCompositeCollector() {
        compositeValueToCnt = new LinkedHashMap<>();
        collectThis = false;
        collector = new VCompositeCollector(compositeValueToCnt);
    }

    private VCompositeCollector(Map<VComposite, VCompositeCnt> compositeValueToCnt) {
        this.compositeValueToCnt = compositeValueToCnt;
        collectThis = true;
        collector = this;
    }

    public Map<VComposite, VCompositeCnt> getCompositeValueToCnt() {
        return compositeValueToCnt;
    }

    private void add(VComposite v) {
        VCompositeCnt old = compositeValueToCnt.get(v);
        if (old != null) {
            old.cnt++;
            old.first.setShared();
            v.setShared();
        } else {
            compositeValueToCnt.put(v, new VCompositeCnt(v));
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
        for (Value ele : value.getList()) {
            ele.accept(collector);
        }
        if (collectThis && !value.getList().isEmpty()) {
            add(value);
        }
    }

    @Override
    public void visit(VMap value) {
        for (Map.Entry<Value, Value> entry : value.getMap().entrySet()) {
            entry.getKey().accept(collector);
            entry.getValue().accept(collector);
        }
        if (collectThis && !value.getMap().isEmpty()) {
            add(value);
        }
    }

    @Override
    public void visit(VBean value) {
        for (Value field : value.getValues()) {
            field.accept(collector);
        }
        if (collectThis && !value.getValues().isEmpty()) {
            add(value);
        }
    }
}
