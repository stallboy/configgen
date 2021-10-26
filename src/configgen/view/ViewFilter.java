package configgen.view;

import configgen.define.AllDefine;
import configgen.define.Bean;
import configgen.define.Column;
import configgen.define.Table;

public interface ViewFilter {

    String name();

    boolean acceptColumn(Column column);

    boolean acceptBean(Bean bean);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean acceptTable(Table table);

    void saveToXml(AllDefine allDefine);

    ViewFilter FULL_DEFINE = new ViewFilter() {

        @Override
        public String name() {
            return "fullDefine";
        }

        @Override
        public boolean acceptColumn(Column column) {
            return true;
        }

        @Override
        public boolean acceptBean(Bean bean) {
            return true;
        }

        @Override
        public boolean acceptTable(Table table) {
            return true;
        }

        @Override
        public void saveToXml(AllDefine allDefine) {

        }

        @Override
        public String toString() {
            return name();
        }
    };


}
