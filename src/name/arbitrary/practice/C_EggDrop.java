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
        for (int i = 0; i < numLines; ++i) {
            C_EggDrop run = new C_EggDrop();
            System.out.println("Case #" + (i+1) + ": " + run.runCase(getString(input)));
        }
    }

    Map<Pair<Integer, Integer>, Long> cache = new HashMap<Pair<Integer, Integer>, Long>();

    private void add(int drops, int breaks, long floors) {
        cache.put(Pair.of(drops, breaks), floors);
    }

    private long get(int drops, int breaks) {
        Long res = cache.get(Pair.of(drops, breaks));
        if (res != null) {
            return res;
        }
        return calcAndCache(drops, breaks);
    }

    private long calcAndCache(int drops, int breaks) {
        System.out.println(drops + " " + breaks); // FIXME
        long floors = calc(drops, breaks);
        add(drops, breaks, floors);
        System.out.println(drops + " " + breaks + " -> " + floors); // FIXME
        return floors;
    }

    private long calc(int drops, int breaks) {
        if (drops <= breaks) {
            // Limiting factor is drops, can binary chop
            return (1 << drops) - 1;
        }
        if (breaks >= 32) {
            // >= 32 drops and breaks means can do binary chop on full 32-bit space.
            return -1;
        }
        if (breaks == 1) {
            // Linear scan
            return drops;
        }
        long caseA = get(drops - 1, breaks - 1);
        if (caseA < 0) {
            return -1;
        }
        long caseB = get(drops - 1, breaks);
        if (caseB < 0) {
            return -1;
        }
        long res = caseA + caseB + 1;
        if (res >= 1L << 32) {
            return -1;
        }
        return res;
    }

    private String runCase(String string) {
        String[] parts = string.split(" ");
        int floors = Integer.parseInt(parts[0]);
        int drops  = Integer.parseInt(parts[1]);
        int breaks = Integer.parseInt(parts[2]);

        System.out.println("A");
        long maxFloors = calc(drops, breaks);
        System.out.println("B");
        int minDrops   = findMinDrops(floors, breaks);
        System.out.println("C");
        int minBreaks  = findMinBreaks(floors, drops);
        System.out.println("D");

        return maxFloors + " " + minDrops + " " + minBreaks;
    }

    private int findMinDrops(long floors, int breaks) {
        int drops = 1;
        while (get(drops, breaks) < floors) {
            drops++;
        }
        return drops;
    }

    private int findMinBreaks(long floors, int drops) {
        int breaks = 1;
        while (get(drops, breaks) < floors) {
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
