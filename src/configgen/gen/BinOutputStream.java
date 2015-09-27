package configgen.gen;

import configgen.value.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;

public class BinOutputStream implements ValueVisitor {
    private final DataOutputStream byter;
    private final Writer texter;
    private int index;
    private byte writeBuffer[] = new byte[8];

    public BinOutputStream(DataOutputStream byter, Writer texter) {
        this.byter = byter;
        this.texter = texter;
    }

    @Override
    public void visit(VBool value) throws IOException {
        addBool(value.value);
    }

    @Override
    public void visit(VInt value) throws IOException {
        addInt(value.value);
    }

    @Override
    public void visit(VLong value) throws IOException {
        addLong(value.value);
    }

    @Override
    public void visit(VFloat value) throws IOException {
        addFloat(value.value);
    }

    @Override
    public void visit(VString value) throws IOException {
        addString(value.value);
    }

    @Override
    public void visit(VText value) throws IOException {
        addText(value.value);
    }

    @Override
    public void visit(VList value) throws IOException {
        addSize(value.list.size());
        value.list.forEach(v -> v.accept(this));
    }

    @Override
    public void visit(VMap value) throws IOException {
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

    public void addText(String text) throws IOException {
        index++;
        texter.write(String.valueOf(index));
        texter.write(",");
        texter.write(escape(text));
        texter.write("\r\n");

        addSize(index);
    }

    public void addBool(boolean v) throws IOException {
        byter.writeBoolean(v);
    }

    public void addSize(int v) throws IOException {
        if (v > 0xFFFF)
            throw new RuntimeException("size > 0xFFFF");
        byter.write((v) & 0xFF);
        byter.write((v >>> 8) & 0xFF);
    }

    public void addInt(int v) throws IOException {
        byter.write((v) & 0xFF);
        byter.write((v >>> 8) & 0xFF);
        byter.write((v >>> 16) & 0xFF);
        byter.write((v >>> 24) & 0xFF);
    }

    public void addLong(long v) throws IOException {
        writeBuffer[0] = (byte) (v);
        writeBuffer[1] = (byte) (v >>> 8);
        writeBuffer[2] = (byte) (v >>> 16);
        writeBuffer[3] = (byte) (v >>> 24);
        writeBuffer[4] = (byte) (v >>> 32);
        writeBuffer[5] = (byte) (v >>> 40);
        writeBuffer[6] = (byte) (v >>> 48);
        writeBuffer[7] = (byte) (v >>> 56);
        byter.write(writeBuffer);
    }

    public void addFloat(float v) throws IOException {
        addInt(Float.floatToIntBits(v));
    }

    public void addString(String v) throws IOException {
        byte[] b = v.getBytes("UTF-8");
        addSize(b.length);
        byter.write(b);
    }

    static String escape(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

}
