package configgen.genlua;

import configgen.value.VBean;
import configgen.value.VComposite;
import configgen.value.VTable;

import java.util.ArrayList;
import java.util.List;

public class ValueShared {

    private final List<ValueSharedLayer> layers = new ArrayList<>();
    private final VTable vTable;

    public ValueShared(VTable vtable) {
        vTable = vtable;
    }

    public void iterateShared() {
        ValueSharedLayer layer1 = new ValueSharedLayer(this);
        for (VBean vBean : vTable.getVBeanList()) {
            vBean.accept(layer1);
        }
        layers.add(layer1);

        ValueSharedLayer currLayer = layer1;
        while (true) {
            ValueSharedLayer nextLayer = new ValueSharedLayer(this);

            for (ValueSharedLayer.VCompositeCnt vc : currLayer.getCompositeValueToCnt().values()) {
                if (!vc.isTraversed()) {
                    vc.getFirst().accept(nextLayer);
                }
            }

            if (!nextLayer.getCompositeValueToCnt().isEmpty()) {
                layers.add(nextLayer);
                currLayer = nextLayer;
            } else {
                break;
            }
        }

    }

    public List<ValueSharedLayer> getLayers() {
        return layers;
    }

    public ValueSharedLayer.VCompositeCnt remove(VComposite v) {
        for (ValueSharedLayer layer : layers) {
            ValueSharedLayer.VCompositeCnt old = layer.getCompositeValueToCnt().remove(v);
            if (old != null) {
                return old;
            }
        }
        return null;
    }

}
