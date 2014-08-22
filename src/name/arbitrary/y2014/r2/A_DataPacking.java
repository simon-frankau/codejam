package name.arbitrary.y2014.r2;

import name.arbitrary.CodeJamBase;

import java.util.*;

public class A_DataPacking extends CodeJamBase {
    A_DataPacking(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new A_DataPacking(args[0]).run();
    }

    @Override
    protected String runCase() {
        String[] parts = getLine().split(" ");
        int n = Integer.parseInt(parts[0]);
        int x = Integer.parseInt(parts[1]);

        String[] sStrings = getLine().split(" ");
        assert n == sStrings.length;
        List<Integer> nums = new ArrayList<Integer>();

        TreeMap<Integer, Integer> counts = new TreeMap<Integer, Integer>();
        for (String sString : sStrings) {
            int i = Integer.parseInt(sString);
            Integer oldCount = counts.get(i);
            counts.put(i, (oldCount == null ? 0 : oldCount) + 1);
        }

        int numDisks = 0;
        while (!counts.isEmpty()) {
            int i = counts.firstKey();
            remove(counts, i);
            NavigableMap<Integer, Integer> shareables = counts.headMap(x - i, true);
            if (!shareables.isEmpty()) {
                remove(counts, shareables.lastKey());
            }
            numDisks++;
        }

        return "" + numDisks;
    }

    void remove(Map<Integer, Integer> counts, int i) {
        int count = counts.get(i);
        if (--count == 0) {
            counts.remove(i);
        } else {
            counts.put(i, count);
        }
    }
}