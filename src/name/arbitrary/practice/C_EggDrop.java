package name.arbitrary.practice;

import com.sun.tools.javac.util.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class C_EggDrop {

    public static void main(String[] args) {
        BufferedReader input = getInput(args[0]);
        int numLines = Integer.parseInt(getString(input));
        C_EggDrop run = new C_EggDrop();
        run.fillAll();
        for (int i = 0; i < numLines; ++i) {
            System.out.println("Case #" + (i+1) + ": " + run.runCase(getString(input)));
        }
    }

    Map<Pair<Integer, Integer>, Long> cache = new HashMap<Pair<Integer, Integer>, Long>();

    private void add(int drops, int breaks, long floors) {
        cache.put(Pair.of(drops, breaks), floors);
    }

    private long get(int drops, int breaks) {
        // First deal with corner cases we don't want to cache...
        if (breaks == 1) {
            // Linear scan
            return drops;
        }
        if (drops <= breaks) {
            // Limited by drops, binary chop
            return (1L << drops) - 1;
        }
        Long res = cache.get(Pair.of(drops, breaks));
        if (res != null) {
            return res;
        }
        return -1;
    }

    private void fillLevel(int breaks) {
        // Skip the part where drops <= breaks, which we don't cache.
        int drops = breaks + 1;
        for (;;) {
            long floors = 1 + get(drops - 1, breaks) + get(drops - 1, breaks - 1);
            if (floors >= (1L << 32)) {
                break;
            }
            add(drops, breaks, floors);
            drops++;
        }
    }

    private void fillAll() {
        // 33 breaks, with binary chop, gets up to 2^32.
        for (int breaks = 2; breaks <= 33; breaks++) {
            fillLevel(breaks);
        }
    }

    private String runCase(String string) {
        String[] parts = string.split(" ");
        int floors = Integer.parseInt(parts[0]);
        int drops  = Integer.parseInt(parts[1]);
        int breaks = Integer.parseInt(parts[2]);

        long maxFloors = get(drops, breaks);
        int minDrops   = findMinDrops(floors, breaks);
        int minBreaks  = findMinBreaks(floors, drops);

        return maxFloors + " " + minDrops + " " + minBreaks;
    }

    private int findMinDrops(long floors, int breaks) {
        int drops = 1;
        while (get(drops, breaks) < floors && get(drops, breaks) != -1) {
            drops++;
        }
        return drops;
    }

    private int findMinBreaks(long floors, int drops) {
        int breaks = 1;
        while (get(drops, breaks) < floors && get(drops, breaks) != -1) {
            breaks++;
        }
        return breaks;
    }

    private static String getString(BufferedReader input) {
        try {
            return input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return "CAN'T HAPPEN";
    }

    private static BufferedReader getInput(String fileName) {
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return input;
    }
}
