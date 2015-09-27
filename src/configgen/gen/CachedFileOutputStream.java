package configgen.gen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CachedFileOutputStream extends ByteArrayOutputStream {
    private static Set<String> filename_set = new HashSet<>();
    private static Set<File> remove_files = new HashSet<>();
    private Path file;

    public CachedFileOutputStream(File file) throws IOException {
        filename_set.add(file.getAbsolutePath().toLowerCase());
        this.file = Paths.get(file.getCanonicalPath());
    }

    private void writeFile() throws IOException {
        Files.write(file, toByteArray(), StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void close() throws IOException {
        if (!file.toFile().exists()) {
            System.out.println("config: create file: " + file);
            writeFile();
        } else if (!Arrays.equals(Files.readAllBytes(file), toByteArray())) {
            System.out.println("config: modify file: " + file);
            writeFile();
        }
    }

    private static void doRemoveFile(File file) {
        String absolutePath = file.getAbsolutePath();
        if (!filename_set.contains(absolutePath.toLowerCase())) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        doRemoveFile(f);
                    }
                }

                files = file.listFiles();
                if (files != null && files.length == 0) {
                    if (file.delete()) {
                        System.out.println("config: delete dir: " + file);
                    } else {
                        System.out.println("config: delete dir fail: " + file);
                    }
                }

            } else {
                if (file.delete()) {
                    System.out.println("config: delete file: " + file);
                } else {
                    System.out.println("config: delete file fail: " + file);
                }
            }

        }
    }

    public static void doRemoveFiles() {
        remove_files.stream().filter(File::exists)
                .forEach(CachedFileOutputStream::doRemoveFile);
        remove_files.clear();
    }

    public static void removeOtherFiles(File file) {
        remove_files.add(file);
    }
}
