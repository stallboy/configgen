package configgen.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class CachedFileOutputStream extends ByteArrayOutputStream {
    private Path path;

    public CachedFileOutputStream(File file) {
        this(file, 512);
    }

    public CachedFileOutputStream(File file, int size) {
        super(size);
        path = file.toPath().toAbsolutePath().normalize();
    }

    @Override
    public void close() throws IOException {
        CachedFiles.writeFile(path, toByteArray());
    }
}
