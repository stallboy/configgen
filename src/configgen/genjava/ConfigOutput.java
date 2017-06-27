package configgen.genjava;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public class ConfigOutput implements Closeable {
    private final DataOutputStream output;

    public ConfigOutput(DataOutputStream output) {
        this.output = output;
    }

    public void writeBool(boolean v) {
        try {
            output.writeBoolean(v);
        } catch (IOException e) {
            throw new ConfigErr(e);
        }
    }

    public void writeInt(int v) {
        try {
            output.writeInt(v);
        } catch (IOException e) {
            throw new ConfigErr(e);
        }
    }

    public void writeLong(long v) {
        try {
            output.writeLong(v);
        } catch (IOException e) {
            throw new ConfigErr(e);
        }
    }

    public void writeFloat(float v) {
        try {
            output.writeFloat(v);
        } catch (IOException e) {
            throw new ConfigErr(e);
        }
    }

    public void writeStr(String v) {
        try {
            output.writeUTF(v);
        } catch (IOException e) {
            throw new ConfigErr(e);
        }
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
}
