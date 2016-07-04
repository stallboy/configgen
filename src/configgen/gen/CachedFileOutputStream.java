package configgen.gen;

import configgen.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class CachedFileOutputStream extends ByteArrayOutputStream {
    private static final Set<String> filename_set = new HashSet<>();
    private Path path;

    public CachedFileOutputStream(File file) throws IOException {
        filename_set.add(fileKey(file));
        path = file.toPath().toAbsolutePath().normalize();
    }

    private void writeFile() throws IOException {
        mkdirs(path.getParent().toFile());
        Files.write(path, toByteArray(), StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void close() throws IOException {
        if (!path.toFile().exists()) {
            Logger.log("create file: " + path);
            writeFile();
        } else if (!Arrays.equals(Files.readAllBytes(path), toByteArray())) {
            Logger.log("modify file: " + path);
            writeFile();
        }
    }

    private static void mkdirs(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Logger.log("mkdirs fail: " + normalizePath(file.toPath()));
            }
        }
    }

    private static String fileKey(File file) {
        return file.toPath().toAbsolutePath().normalize().toString().toLowerCase();
    }

    private static String normalizePath(Path path) {
        return path.toAbsolutePath().normalize().toString();
    }

    private static void delete(File file) {
        String dir = file.isDirectory() ? "dir" : "file";
        String ok = file.delete() ? "" : " fail";
        Logger.log("delete " + dir + ok + ": " + normalizePath(file.toPath()));
    }

    private static void doRemoveFile(File file, boolean keepMeta) {
        String key = fileKey(file);
        boolean keep = filename_set.contains(key);
        if (!keep) {
            if (keepMeta && key.endsWith(".meta")) {
                String noMetaKey = key.substring(0, key.length() - 5);
                keep = filename_set.contains(noMetaKey);
                if (!keep && new File(noMetaKey).isDirectory()) {
                    for (String f : filename_set) {
                        if (f.startsWith(noMetaKey)){
                            keep = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!keep) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        doRemoveFile(f, keepMeta);
                    }
                }
                File[] newFiles = file.listFiles();
                if (newFiles != null && newFiles.length == 0) {
                    delete(file);
                }
            } else {
                delete(file);
            }
        }
    }

    private static List<File> deleteFiles = new ArrayList<>();
    private static List<File> deleteKeepMetaFiles = new ArrayList<>();

    public static void deleteOtherFiles(File file) {
        deleteFiles.add(file);
    }

    public static void keepMetaAndDeleteOtherFiles(File file) {
        deleteKeepMetaFiles.add(file);
    }

    public static void finalExit() {
        deleteFiles.stream().filter(File::exists)
                .forEach(f -> doRemoveFile(f, false));
        deleteKeepMetaFiles.stream().filter(File::exists)
                .forEach(f -> doRemoveFile(f, true));
    }
}
