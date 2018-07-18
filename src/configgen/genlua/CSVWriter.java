package configgen.genlua;

import java.io.*;
import java.util.List;

public class CSVWriter {

    /* https://tools.ietf.org/html/rfc4180
   6.  Fields containing line breaks (CRLF), double quotes, and commas
       should be enclosed in double-quotes.  For example:

       "aaa","b CRLF
       bb","ccc" CRLF
       zzz,yyy,xxx

   7.  If double-quotes are used to enclose fields, then a double-quote
       appearing inside a field must be escaped by preceding it with
       another double quote.  For example:

       "aaa","b""bb","ccc"
       */
    public static void write(Writer writer, List<List<String>> rows) throws IOException {
        if (rows.isEmpty()) {
            return;
        }
        int columnCount = rows.get(0).size();

        for (int r = 0; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            if (row.size() != columnCount) {
                throw new IllegalArgumentException("csv里每行数据个数应该相同，但这里第" + r + "行，数据有" + row.size() + "个,跟第一行" + columnCount + ",个数不符合");
            }

            for (int c = 0; c < row.size(); c++) {
                String cell = row.get(c);
                boolean enclose = false;
                if (cell.contains("\"")) {
                    cell = cell.replace("\"", "\"\"");
                    enclose = true;
                } else if (cell.contains("\r\n") || cell.contains(",")) {
                    enclose = true;
                } else if (cell.contains("\r") || cell.contains("\n")){ //这个是为了兼容excel，不是rfc4180的要求
                    enclose = true;
                }

                if (enclose) {
                    cell = "\"" + cell + "\"";
                }


                writer.write(cell);
                if (c != row.size() - 1) {
                    writer.write(",");
                } else {
                    writer.write("\r\n");
                }
            }
        }
    }


    public static void writeToFile(File file, String encoding, List<List<String>> rows) {
        try {
            try(Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding))){
                write(w, rows);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
