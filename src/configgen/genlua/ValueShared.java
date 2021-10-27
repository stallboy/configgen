package configgen.genlua;

import configgen.value.VBean;
import configgen.value.VComposite;
import configgen.value.VTable;

import java.util.ArrayList;
import java.util.List;

class ValueShared {

    private final List<ValueSharedLayer> layers = new ArrayList<>();
    private final VTable vTable;

    ValueShared(VTable vtable) {
        vTable = vtable;
    }

    void iterateShared() {
        ValueSharedLayer layer1 = new ValueSharedLayer(this);
        for (VBean vBean : vTable.getVBeanList()) {
            vBean.accept(layer1);
        }
        layers.add(layer1);

        ValueSharedLayer currLayer = layer1;
        while (true) {
            ValueSharedLayer nextLayer = new ValueSharedLayer(this);

            List<ValueSharedLayer.VCompositeCnt> currLayerCopy = new ArrayList<>(currLayer.getCompositeValueToCnt().values());
            for (ValueSharedLayer.VCompositeCnt vc : currLayerCopy) {
                if (!vc.isTraversed()) {
                    vc.getFirst().accept(nextLayer);
                    vc.setTraversed();
                }
            }

            if (!nextLayer.getCompositeValueToCnt().isEmpty()) {
                layers.add(nextLayer);
                currLayer = nextLayer;
            } else {
                break;  // 直到下一层，再也没有共用的结构了，就退出
            }
        }
    }

    List<ValueSharedLayer> getLayers() {
        return layers;
    }

    ValueSharedLayer.VCompositeCnt remove(VComposite v) {
        for (ValueSharedLayer layer : layers) {
            ValueSharedLayer.VCompositeCnt old = layer.getCompositeValueToCnt().remove(v);
            if (old != null) {
                return old;
            }
        }
        return null;
    }

}
