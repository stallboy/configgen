package configgen;

import configgen.define.*;
import configgen.type.TList;
import configgen.type.TMap;
import configgen.type.Type;
import configgen.type.TBool;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

public class CSVWriter {
    private final DataOutputStream byter;
    private final Writer texter;
    private int index;
    private byte writeBuffer[] = new byte[8];

    public CSVWriter(DataOutputStream byter, Writer texter) {
        this.byter = byter;
        this.texter = texter;
    }

    public void addConfig(String config, boolean hasText) throws IOException {
        index = 0;
        if (hasText) {
            texter.write(escape("#" + config));
            texter.write("\r\n");
        }

        addString(config);
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

    public void addConfig(Config config) throws IOException {
        addConfig(config.getName(), config.getBean().HASTEXT());
        addSize(config.getData().getRowList().size());

        for (List<String> row : config.getData().getRowList()) {
            List<String> orderRow = config.getColumnList().stream().map(row::get).collect(Collectors.toList());
            addBean(config.getBean(), orderRow);
        }
    }

    private void addType(Type t, List<String> data) throws IOException {
        if (t instanceof TBool){
            addTypePrimitive((TBool) t, data.get(0));
        }else if (t instanceof Bean){
            addBean((Bean) t, data);
        }else if (t instanceof TList){
            addTypeList((TList) t, data);
        }else if (t instanceof TMap){
            addTypeMap((TMap) t, data);
        }
    }

    private void addTypePrimitive(TBool type, String data) throws IOException {
        switch (type) {
            case BOOL:
                addBool(CSV.parseBoolean(data));
                break;
            case INT:
                addInt(CSV.parseInt(data));
                break;
            case LONG:
                addLong(CSV.parseLong(data));
                break;
            case FLOAT:
                addFloat(CSV.parseFloat(data));
                break;
            case STRING:
                addString(data);
                break;
            case TEXT:
                addText(data);
                break;
        }
    }

    private void addBean(Bean type, List<String> data) throws IOException {
        if (type.isCompress()) {
            data = CSV.parseList(data.get(0));
        }

        int start = 0;
        for (Field f : type.getFields()) {
            Type ft = f.getType();
            int end = start + ft.columnSpan();
            if (f.ISOWN()) {
                addType(ft, data.subList(start, end));
            }
            start = end;
        }
    }

    private void addTypeList(TList type, List<String> data) throws IOException {
        if (type.isCompress()) {
            List<String> list = CSV.parseList(data.get(0));
            addSize(list.size());
            for (String e : list) {
                addTypePrimitive((TBool) type.value, e);
            }

        } else {
            int vs = type.value.columnSpan();
            int count = 0;
            for (int i = 0; i < type.count; i++) {
                if (!data.get(i * vs).isEmpty()) {
                    count++;
                }
            }
            addSize(count);
            for (int i = 0; i < type.count; i++) {
                int b = i * vs;
                if (!data.get(b).isEmpty()) {
                    addType(type.value, data.subList(b, b + vs));
                }
            }
        }
    }

    private void addTypeMap(TMap type, List<String> data) throws IOException {
        int ks = type.key.columnSpan();
        int vs = type.value.columnSpan();
        int kvs = ks + vs;
        int count = 0;
        for (int i = 0; i < type.count; i++) {
            if (!data.get(i * kvs).isEmpty()) {
                count++;
            }
        }

        addSize(count);
        for (int i = 0; i < type.count; i++) {
            int b = i * kvs;
            if (!data.get(b).isEmpty()) {
                addType(type.key, data.subList(b, b + ks));
                addType(type.value, data.subList(b + ks, b + kvs));
            }
        }
    }

    static String escape(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

}
