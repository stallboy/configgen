package configgen.util;

import configgen.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class CachedFiles {
    private static final Set<String> filename_set = new HashSet<>();

    private static List<File> deleteFiles = new ArrayList<>();
    private static List<File> deleteKeepMetaFiles = new ArrayList<>();

    public static void deleteOtherFiles(File dir) {
        deleteFiles.add(dir);
    }

    public static void keepMetaAndDeleteOtherFiles(File dir) {
        deleteKeepMetaFiles.add(dir);
    }

    public static void finalExit() {
        deleteFiles.stream().filter(File::exists)
                .forEach(f -> doRemoveFile(f, false));
        deleteKeepMetaFiles.stream().filter(File::exists)
                .forEach(f -> doRemoveFile(f, true));
    }

    static void writeFile(Path path, byte[] data) throws IOException {
        filename_set.add(fileKey(path));
        if (!path.toFile().exists()) {
            Logger.log("create file: " + path);
            mkdirs(path.getParent().toFile());
            Files.write(path, data, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            return;
        }

        int nread = FileReadUtils.readAllBytes(path);
        byte[] buf = FileReadUtils.getBuf();
        if (!arrayEquals(buf, nread, data, data.length)){
            Logger.log("modify file: " + path);
            Files.write(path, data, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private static boolean arrayEquals(byte[] a, int aSize, byte[] a2, int a2Size) {
        if (a == a2)
            return true;

        if (aSize != a2Size)
            return false;

        for (int i = 0; i < aSize; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    private static void mkdirs(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Logger.log("mkdirs fail: " + normalizePath(file.toPath()));
            }
        }
    }

    private static String fileKey(Path path) {
        return path.toAbsolutePath().normalize().toString().toLowerCase();
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
        String key = fileKey(file.toPath());
        boolean keep = filename_set.contains(key);
        if (!keep) {
            if (keepMeta && key.endsWith(".meta")) {
                String noMetaKey = key.substring(0, key.length() - 5);
                keep = filename_set.contains(noMetaKey);
                if (!keep && new File(noMetaKey).isDirectory()) {
                    for (String f : filename_set) {
                        if (f.startsWith(noMetaKey)) {
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


}
