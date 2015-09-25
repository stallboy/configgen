package configgen.value;

import configgen.Node;
import configgen.data.Data;
import configgen.type.Cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CfgV extends Node {
    public final Cfg type;
    public final Data data;

    public final List<VBean> vbeans = new ArrayList<>();

    public CfgV(CfgVs parent, String link, Cfg cfg, Data data) {
        super(parent, link);
        this.type = cfg;
        this.data = data;

        List<Integer> cols = new ArrayList<>();
        cfg.tbean.fields.forEach((name, type) -> cols.addAll(data.columns.get(name).indexs));

        data.line2data.forEach((row, rowData) -> {
            List<Cell> order = cols.stream().map(col -> new Cell(row, col, rowData.get(col))).collect(Collectors.toList());
            vbeans.add(new VBean(this, "", cfg.tbean, order));
        });
    }

}
