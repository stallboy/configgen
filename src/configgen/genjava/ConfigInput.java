package configgen.genjava;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;

public class ConfigInput implements Closeable {
    private final DataInputStream input;

    public ConfigInput(DataInputStream input) {
        this.input = input;
    }

    public boolean readBool() {
        try {
            return input.readBoolean();
        } catch (IOException e) {
            throw new ConfigErr(e);
        }
    }

    public int readInt() {
        try {
            return input.readInt();
        } catch (IOException e) {
            throw new ConfigErr(e);
        }
    }

    public long readLong() {
        try {
            return input.readLong();
        } catch (IOException e) {
            throw new ConfigErr(e);
        }
    }

    public float readFloat() {
        try {
            return input.readFloat();
        } catch (IOException e) {
            throw new ConfigErr(e);
        }
    }

    public String readStr() {
        try {
            return input.readUTF();
        } catch (IOException e) {
            throw new ConfigErr(e);
        }
    }

    public int skipBytes(int n) {
        try {
            return input.skipBytes(n);
        } catch (IOException e) {
            throw new ConfigErr(e);
        }
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
