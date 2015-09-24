package configgen;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class CSV {

    private static final char comma = ',';
    private static final char quote = '"';
    private static final char cr = '\r';
    private static final char lf = '\n';

    private enum State {
        START, NOQUOTE, QUOTE, QUOTE2, CR,
    }

    public static List<List<String>> parse(Reader reader, boolean removeEmptyLine) throws IOException {
        List<List<String>> result = new ArrayList<>();
        List<String> record = new ArrayList<>();
        State state = State.START;
        StringBuilder field = null;

        for (int i = reader.read(); i != -1; i = reader.read()) {
            char c = (char) i;

            switch (state) {
                case START:
                    switch (c) {
                        case comma:
                            record.add("");
                            break;
                        case quote:
                            field = new StringBuilder();
                            state = State.QUOTE;
                            break;
                        case cr:
                            field = new StringBuilder();
                            state = State.CR;
                            break;
                        default:
                            field = new StringBuilder();
                            field.append(c);
                            state = State.NOQUOTE;
                            break;
                    }
                    break;

                case NOQUOTE:
                    switch (c) {
                        case comma:
                            record.add(field.toString());
                            state = State.START;
                            break;
                        case cr:
                            state = State.CR;
                            break;
                        default:
                            field.append(c);
                            break;
                    }
                    break;

                case QUOTE:
                    switch (c) {
                        case quote:
                            state = State.QUOTE2;
                            break;
                        default:
                            field.append(c);
                            break;
                    }
                    break;

                case QUOTE2:
                    switch (c) {
                        case comma:
                            record.add(field.toString());
                            state = State.START;
                            break;
                        case quote:
                            field.append(quote);
                            state = State.QUOTE;
                            break;
                        case cr:
                            state = State.CR;
                            break;
                        default:
                            field.append(c);
                            state = State.NOQUOTE;
                            break;
                    }
                    break;

                case CR:
                    switch (c) {
                        case comma:
                            field.append(cr);
                            record.add(field.toString());
                            state = State.START;
                            break;
                        case lf:
                            record.add(field.toString());
                            result.add(record);
                            record = new ArrayList<>();
                            state = State.START;
                            break;
                        default:
                            field.append(cr);
                            field.append(c);
                            state = State.NOQUOTE;
                            break;
                    }
                    break;
            }
        }

        switch (state) {
            case START:
                if (!record.isEmpty()) {
                    record.add("");
                    result.add(record);
                }
                break;
            case CR:
                field.append(cr);
            default:
                record.add(field.toString());
                result.add(record);
                break;
        }

        if (removeEmptyLine) {
            return result.stream().filter(line -> !isEmptyLine(line)).collect(Collectors.toList());
        } else {
            return result;
        }
    }

    public static boolean isEmptyLine(List<String> line) {
        for (String s : line) {
            if (!s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static final char semicolon = ';';

    private enum ListState {
        START, NOQUOTE, QUOTE, QUOTE2
    }

    public static List<String> parseList(String str) {
        ListState state = ListState.START;
        List<String> list = new ArrayList<>();
        StringBuilder field = null;

        for (char c : str.toCharArray()) {
            switch (state) {
                case START:
                    switch (c) {
                        case semicolon:
                            list.add("");
                            break;
                        case quote:
                            field = new StringBuilder();
                            state = ListState.QUOTE;
                            break;
                        default:
                            field = new StringBuilder();
                            field.append(c);
                            state = ListState.NOQUOTE;
                            break;
                    }
                    break;

                case NOQUOTE:
                    switch (c) {
                        case semicolon:
                            list.add(field.toString());
                            state = ListState.START;
                            break;
                        default:
                            field.append(c);
                            break;
                    }
                    break;

                case QUOTE:
                    switch (c) {
                        case quote:
                            state = ListState.QUOTE2;
                            break;
                        default:
                            field.append(c);
                            break;
                    }
                    break;

                case QUOTE2:
                    switch (c) {
                        case semicolon:
                            list.add(field.toString());
                            state = ListState.START;
                            break;
                        case quote:
                            field.append(quote);
                            state = ListState.QUOTE;
                            break;
                        default:
                            field.append(c);
                            state = ListState.NOQUOTE;
                            break;
                    }
                    break;
            }
        }

        switch (state) {
            case START:
                break;
            default:
                list.add(field.toString());
                break;
        }

        return list;
    }

    public static boolean parseBoolean(String s) {
        String t = s.trim();
        return !t.isEmpty() && (t.equals("1") || t.equalsIgnoreCase("true"));
    }

    public static float parseFloat(String s) {
        String t = s.trim();
        return t.isEmpty() ? 0.f : Float.parseFloat(t);
    }

    public static int parseInt(String s) {
        String t = s.trim();
        return t.isEmpty() ? 0 : Integer.decode(t);
    }

    public static long parseLong(String s) {
        String t = s.trim();
        return t.isEmpty() ? 0 : Long.parseLong(t);
    }

    static String path2Name(String p) {
        String[] res = p.split("\\\\|/");
        if (res.length > 0) {
            String last = res[res.length - 1];
            res[res.length - 1] = last.substring(0, last.length() - 4);
        }
        return String.join(".", res);
    }

    private static String[] name2ClassPath(String name) {
        String[] seps = name.split("\\.");
        if (seps.length > 0) {
            String c = seps[seps.length - 1];
            seps[seps.length - 1] = c.substring(0, 1).toUpperCase() + c.substring(1).toLowerCase();
        }
        return seps;
    }

    @SuppressWarnings("unused")
    static Set<String> load(Path zipPath, String charsetName) throws Exception {
        Set<String> loaded = new HashSet<>();
        String packageName = CSV.class.getPackage().getName();
        try (ZipInputStream zis = new ZipInputStream(new CheckedInputStream(new FileInputStream(zipPath.toFile()), new CRC32()))) {
            ZipEntry entry;
            Collection<Class<?>> classList = new ArrayList<>();
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".csv")) {
                    String name = path2Name(entry.getName());
                    try {
                        String clzname = packageName + "." + String.join(".", name2ClassPath(name));
                        Class<?> clz = Class.forName(clzname);
                        if (clz != null) {
                            classList.add(clz);
                            Method initialize = clz.getDeclaredMethod("initialize", List.class);
                            initialize.setAccessible(true);
                            List<List<String>> res = parse(new BufferedReader(new InputStreamReader(zis, charsetName)), true);
                            initialize.invoke(null, res.subList(2, res.size()));
                            loaded.add(name);
                        }
                    } catch (ClassNotFoundException ignore) {
                    }
                }
            }

            for (Class<?> clz : classList) {
                try {
                    Method resolve = clz.getDeclaredMethod("resolve");
                    resolve.setAccessible(true);
                    resolve.invoke(null);
                } catch (NoSuchMethodException ignore) {
                }
            }
        }
        return loaded;
    }
}
