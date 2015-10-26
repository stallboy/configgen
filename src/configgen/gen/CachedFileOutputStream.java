package configgen.gen;

import configgen.Logger;

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
    private static final Set<String> filename_set = new HashSet<>();
    private Path file;

    public CachedFileOutputStream(File file) throws IOException {
        filename_set.add(file.getAbsolutePath().toLowerCase());
        this.file = Paths.get(file.getCanonicalPath());
    }

    private void writeFile() throws IOException {
        mkdirs(file.getParent().toFile());
        Files.write(file, toByteArray(), StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void close() throws IOException {
        if (!file.toFile().exists()) {
            Logger.log("create file: " + file);
            writeFile();
        } else if (!Arrays.equals(Files.readAllBytes(file), toByteArray())) {
            Logger.log("modify file: " + file);
            writeFile();
        }
    }

    private static void mkdirs(File path) {
        if (!path.exists()) {
            if (!path.mkdirs()) {
                Logger.log("mkdirs fail: " + path.toPath().toAbsolutePath().normalize());
            }
        }
    }

    private static void delete(File file) {
        String dir = file.isDirectory() ? "dir" : "file";
        String ok = file.delete() ? "" : " fail";
        Logger.log("delete " + dir + ok + ": " + file.toPath().toAbsolutePath().normalize());
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
                    delete(file);
                }
            } else {
                delete(file);
            }
        }
    }

    public static void deleteOtherFiles(File... files) {
        Arrays.asList(files).stream().filter(File::exists)
                .forEach(CachedFileOutputStream::doRemoveFile);
    }
}
