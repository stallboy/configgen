package configgen.genjava;

import java.io.DataInput;
import java.io.IOException;

public class ConfigInput {
    private final DataInput input;

    public ConfigInput(DataInput input) {
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
}
