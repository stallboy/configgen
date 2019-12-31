package configgen.genlua;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

//　对bool,int字段的列模式压缩信息
class PackInfo {
    private int bitLen;
    private int countPerOne;


    PackInfo(int bitLen) {
        this.bitLen = bitLen;
        this.countPerOne = 53 / bitLen;
    }

    private List<Long> result = new ArrayList<>();
    private BitSet curBs = new BitSet();
    private int nextSlot = 0;


    int getBitLen() {
        return bitLen;
    }

    void addBool(boolean v) {
        if (nextSlot == countPerOne) {
            toResult();
        }

        if (v) {
            curBs.set(nextSlot);
        }
        nextSlot++;
    }

    private static long[] cache = new long[1];

    void addInt(int v) {
        if (nextSlot == countPerOne) {
            toResult();
        }

        cache[0] = v;
        BitSet tmp = BitSet.valueOf(cache);
        for (int i = 0; i < bitLen; i++) {
            if (tmp.get(i)) {
                curBs.set(nextSlot * bitLen + i);
            }
        }
        nextSlot++;
    }

    void packTo(StringBuilder res) {
        toResult();
        int idx = 0;
        int cnt = result.size();
        for (Long v : result) {
            res.append(v);
            idx++;
            if (idx < cnt) {
                res.append(",");
            }
        }
    }


    private void toResult() {
        long curV = 0;
        if (curBs.length() > 0) {
            curV = curBs.toLongArray()[0];
        }
        result.add(curV);
        curBs.clear();
        nextSlot = 0;
    }


}
