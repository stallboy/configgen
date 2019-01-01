package configgen.util;

public class BomChecker {

    public static class Res {
        public String encoding;
        public int bomSize;

        public Res(String encoding, int bomSize) {
            this.encoding = encoding;
            this.bomSize = bomSize;
        }
    }

    public static Res checkBom(byte[] bom, int size, String defaultEncoding) {
        if ((size > 3 && bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) &&
                (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
            return new Res("UTF-32BE", 4);
        } else if ((size > 3 && bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) &&
                (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
            return new Res("UTF-32LE", 4);
        } else if (size > 2 && (bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) &&
                (bom[2] == (byte) 0xBF)) {
            return new Res("UTF-8", 3);
        } else if (size > 1 && (bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            return new Res("UTF-16BE", 2);
        } else if (size > 1 && (bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            return new Res("UTF-16LE", 2);
        } else {
            return new Res(defaultEncoding, 0);
        }
    }
}
