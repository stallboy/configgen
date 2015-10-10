package configgen.gen;

import configgen.value.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;

public class BinOutputStream implements ValueVisitor {
    private final DataOutputStream byter;
    private final Writer texter;
    private int index;
    private final byte[] writeBuffer = new byte[8];

    public BinOutputStream(DataOutputStream byter, Writer texter) {
        this.byter = byter;
        this.texter = texter;
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
        switch (value.tstring.subtype) {
            case STRING:
                addString(value.value);
                break;
            case TEXT:
                addText(value.value);
                break;
        }
    }

    @Override
    public void visit(VList value) {
        addSize(value.list.size());
        value.list.forEach(v -> v.accept(this));
    }

    @Override
    public void visit(VMap value) {
        addSize(value.map.size());
        value.map.forEach((k, v) -> {
            k.accept(this);
            v.accept(this);
        });
    }

    @Override
    public void visit(VBean value) {
        value.map.values().forEach(v -> v.accept(this));
    }

    public void addCfgV(CfgV cfgv) throws IOException {
        index = 0;
        if (cfgv.type.tbean.hasText()) {
            texter.write(escape("#" + cfgv.type.tbean.define.name));
            texter.write("\r\n");
        }
        addString(cfgv.type.tbean.define.name);
        addSize(cfgv.vbeans.size());
        cfgv.vbeans.forEach(v -> v.accept(this));
    }

    private void addText(String text) {
        index++;
        try {
            texter.write(String.valueOf(index));
            texter.write(",");
            texter.write(escape(text));
            texter.write("\r\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        addSize(index);
    }

    private void addBool(boolean v) {
        try {
            byter.writeBoolean(v);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addSize(int v) {
        if (v > 0xFFFF)
            throw new RuntimeException("size > 0xFFFF");
        try {
            byter.write((v) & 0xFF);
            byter.write((v >>> 8) & 0xFF);
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
            byte[] b = v.getBytes("UTF-8");
            addSize(b.length);
            byter.write(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String escape(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

}
