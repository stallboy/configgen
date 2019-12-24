package configgen.tool;

import configgen.value.*;

import java.util.Map;
import java.util.Set;

public class ValueSearcher {

    public static void searchValues(AllValue allValue, Set<Integer> searchIntegers) {
        ValueVisitor vv = new ValueVisitor() {
            @Override
            public void visit(VBool value) {

            }

            @Override
            public void visit(VInt value) {
                if (searchIntegers.contains(value.value)) {
                    System.out.println(value.getType().fullName() + ": " + value.value);
                }
            }

            @Override
            public void visit(VLong value) {
                if (searchIntegers.contains((int) value.value)) {
                    System.out.println(value.getType().fullName() + ": " + value.value);
                }
            }

            @Override
            public void visit(VFloat value) {

            }

            @Override
            public void visit(VString value) {

            }

            @Override
            public void visit(VList value) {
                for (Value value1 : value.getList()) {
                    value1.accept(this);
                }
            }

            @Override
            public void visit(VMap value) {
                for (Map.Entry<Value, Value> value1 : value.getMap().entrySet()) {
                    value1.getKey().accept(this);
                    value1.getValue().accept(this);
                }
            }

            @Override
            public void visit(VBean value) {
                for (Value valueValue : value.getValues()) {
                    valueValue.accept(this);
                }
            }
        };

        for (VTable vTable : allValue.getVTables()) {
            for (VBean vBean : vTable.getVBeanList()) {
                vBean.accept(vv);
            }
        }
    }
}
