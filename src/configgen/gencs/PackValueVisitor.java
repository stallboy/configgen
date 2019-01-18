package configgen.gencs;

import configgen.value.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class PackValueVisitor implements ValueVisitor {
    private final DataOutputStream byter;
    private final byte[] writeBuffer = new byte[8];

    PackValueVisitor(OutputStream _byter) {
        this.byter = new DataOutputStream(_byter);
    }

    @Override
    public void visit(VBool value) {
        addBool(value.value);
    }

    @Override
    public void visit(VInt value) {
        addInt(value.value);
    }

    @Override
    public void visit(VLong value) {
        addLong(value.value);
    }

    @Override
    public void visit(VFloat value) {
        addFloat(value.value);
    }

    @Override
    public void visit(VString value) {
        addString(value.value);
    }

    @Override
    public void visit(VList value) {
        addInt(value.getList().size());
        for (Value v : value.getList()) {
            v.accept(this);
        }
    }

    @Override
    public void visit(VMap value) {
        addInt(value.getMap().size());
        value.getMap().forEach((k, v) -> {
            k.accept(this);
            v.accept(this);
        });
    }

    @Override
    public void visit(VBean value) {
        if (value.getChildDynamicVBean() != null) {
            addString(value.getChildDynamicVBean().getTBean().name);
            for (Value v : value.getChildDynamicVBean().getValues()) {
                v.accept(this);
            }
        }else{
            for (Value v : value.getValues()) {
                v.accept(this);
            }
        }
    }

    void addVTable(VTable vtable) {
        addString(vtable.getTTable().getTBean().getBeanDefine().name);
        addInt(vtable.getVBeanList().size());
        for (VBean v : vtable.getVBeanList()) {
            v.accept(this);
        }
    }

    private void addBool(boolean v) {
        try {
            byter.writeBoolean(v);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addInt(int v) {
        try {
            byter.write((v) & 0xFF);
            byter.write((v >>> 8) & 0xFF);
            byter.write((v >>> 16) & 0xFF);
            byter.write((v >>> 24) & 0xFF);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addLong(long v) {
        writeBuffer[0] = (byte) (v);
        writeBuffer[1] = (byte) (v >>> 8);
        writeBuffer[2] = (byte) (v >>> 16);
        writeBuffer[3] = (byte) (v >>> 24);
        writeBuffer[4] = (byte) (v >>> 32);
        writeBuffer[5] = (byte) (v >>> 40);
        writeBuffer[6] = (byte) (v >>> 48);
        writeBuffer[7] = (byte) (v >>> 56);
        try {
            byter.write(writeBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addFloat(float v) {
        addInt(Float.floatToIntBits(v));
    }

    private void addString(String v) {
        try {
            byte[] b = v.getBytes(StandardCharsets.UTF_8);
            addInt(b.length);
            byter.write(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
