package configgen.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileNameExtract {

    public static String extractFileName(String fileNameNoExt) {
        if (fileNameNoExt.isEmpty()) {
            return null;
        }

        // 只接受首字母是英文字母的页签
        char firstChar = fileNameNoExt.charAt(0);
        boolean startWithAZ = ('a' <= firstChar && firstChar <= 'z') || ('A' <= firstChar && firstChar <= 'Z');
        if (!startWithAZ) {
            return null;
        }

        int hanIdx = findFirstHanIndex(fileNameNoExt);
        if (hanIdx == -1) {
            return fileNameNoExt.toLowerCase();
        }

        int end = hanIdx;
        if (fileNameNoExt.charAt(hanIdx - 1) == '_') {
            end = hanIdx - 1;
        }
        return fileNameNoExt.substring(0, end).toLowerCase();
    }

    public static String extractPathName(String pathName) {
        if (pathName.isEmpty()) {
            return "";
        }

        String[] split = pathName.split("[\\\\/]");
        String[] normalized = new String[split.length];

        for (int i = 0; i < split.length; i++) {
            String e = extractFileName(split[i]);
            if (e == null){
                throw new RuntimeException(pathName + " 不符合规范");
            }
            normalized[i] = e;
        }
        return String.join(".", normalized).toLowerCase();
    }

    public static Path packageNameToPathName(Path rootDir, String packageName) {
        String[] split = packageName.split("\\.");

        Path curPath = rootDir;
        for (String p : split) {
            try {
                curPath = Files.list(curPath).filter(path -> path.toFile().isDirectory()
                                && isFileNameExtractMatch(path.getFileName().toString(), p))
                        .findFirst().orElse(curPath.resolve(p));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return curPath;
    }


    public static boolean isFileNameExtractMatch(String fileNameNoExt, String extracted) {
        if (!fileNameNoExt.toLowerCase().startsWith(extracted)) {
            return false;
        }

        String left = fileNameNoExt.substring(extracted.length());

        if (left.length() == 0) {
            return true;
        }

        int hanIdx = findFirstHanIndex(left);
        return hanIdx == 0 || (hanIdx == 1 && left.charAt(0) == '_');
    }

    static int findFirstHanIndex(String s) {
        for (int i = 0; i < s.length(); ) {
            int codepoint = s.codePointAt(i);
            if (Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN) {
                return i;
            }
            i += Character.charCount(codepoint);
        }
        return -1;
    }

}
