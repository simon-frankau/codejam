package name.arbitrary.y2014.r1c;

import com.sun.tools.javac.util.Pair;
import name.arbitrary.CodeJamBase;

import java.util.*;

// 20:56 - 21:29 - Spent time solving wrong problem
// 21:29 - 22:52 - Got an algorithm, but some bug stops it working on small set. Taking a break.
// Next day: D'oh, forgot to spot cycles AB BA
public class B_ReorderingTrainCars extends CodeJamBase {
    B_ReorderingTrainCars(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new B_ReorderingTrainCars(args[0]).run();
    }

    @Override
    protected String runCase() {
        int n = Integer.parseInt(getLine());
        String[] parts = getLine().split(" ");
        // First, tidy up strings.
        for (int i = 0; i < parts.length; i++) {
            parts[i] = dedup(parts[i]);
        }

        System.err.println(Arrays.asList(parts));

        // Check that no carriage has the pattern ABA
        for (String part : parts) {
            if (part.length() > 2 && (part.charAt(0) == part.charAt(part.length() - 1))) {
                System.err.println("ABA carriage");
                return "0";
            }
        }

        // Now, get the edges of carriages, and pick up the middles.
        List<Character> middles = new ArrayList<Character>();
        Set<Character> edges = new HashSet<Character>();
        List<Pair<Character, Character>> cars = new ArrayList<Pair<Character, Character>>();

        splitOut(parts, cars, middles, edges);
        Set<Character> middleSet = new HashSet<Character>(middles);
        if (middleSet.size() != middles.size()) {
            // Same character occurs in multiple middles - can't be contiguous.
            System.err.println("Character in multiple middles");
            return "0";
        }

        middleSet.retainAll(edges);
        if (!middleSet.isEmpty()) {
            // Character in middle can't be contiguous with character on edge of some other block.
            System.err.println("Middle matches edge");
            return "0";
        }

        return mainCase(cars);
    }

    // This bit does the main of processing.
    private String mainCase(List<Pair<Character, Character>> cars) {
        long combos = 1; // Multiplicative factor coming from reordering blocks with same value on either side.
        int chains = 0; // Number of chains we've identified.

        // First, pull out the single character trains.
        Map<Character, Integer> constTrainCounts = new HashMap<Character, Integer>();

        Iterator<Pair<Character, Character>> i = cars.iterator();
        while (i.hasNext()) {
            Pair<Character, Character> car = i.next();
            if (car.fst == car.snd) {
                i.remove();
                Integer prev = constTrainCounts.get(car.fst);
                constTrainCounts.put(car.fst, (prev == null ? 0 : prev) + 1);
            }
        }

        Set<Character> edges = new HashSet<Character>();
        for (Pair<Character, Character> pair : cars) {
            edges.add(pair.fst);
            edges.add(pair.snd);
        }

        for (Map.Entry<Character, Integer> entry : constTrainCounts.entrySet()) {
            // If a constant character chain is not forced to be joined to anything else, it forms its own chain.
            if (!edges.contains(entry.getKey())) {
                System.err.println(entry.getKey() + " unconnected (" + entry.getValue() + ")");
                chains++;
            } else {
                System.err.println(entry.getKey() + " connected (" + entry.getValue() + ")");
            }
            combos *= factorial(entry.getValue());
            combos %= 1000000007L;
        }

        if (hasCycles(cars)) {
            System.err.println("Has a cycle");
            return "0";
        }

        List<Character> startsList = new ArrayList<Character>();
        List<Character> endsList   = new ArrayList<Character>();
        for (Pair<Character, Character> pair : cars) {
            startsList.add(pair.fst);
            endsList.add(pair.snd);
        }
        Set<Character> starts = new HashSet<Character>(startsList);
        Set<Character> ends   = new HashSet<Character>(endsList);

        if (starts.size() != startsList.size()) {
            System.err.println("Repeated start");
            return "0";
        }

        if (ends.size() != endsList.size()) {
            System.err.println("Repeated end");
            return "0";
        }

        starts.removeAll(ends);
        System.err.println("Starts: " + starts);
        chains += starts.size();

        System.err.println("Chains: " + chains + " Combos: " + combos);

        long result = ((factorial(chains) * combos) % 1000000007L);
        System.err.println("Result: " + result);
        return "" + result;
    }

    private boolean hasCycles(List<Pair<Character, Character>> cars) {
        Map<Character, Character> chain = new HashMap<Character, Character>();
        for (Pair<Character, Character> car : cars) {
            chain.put(car.fst, car.snd);
        }
        for (char c : chain.keySet()) {
            Character curr = c;
            do {
                curr = chain.get(curr);
                if (curr != null && curr == c) {
                    // Found a cycle
                    return true;
                }
            } while (curr != null);
        }
        return false;
    }

    long factorial(int n) {
        long l = 1;
        for (int i = 1; i <= n; i++) {
            l *= i;
            l %= 1000000007L;
        }
        return l;
    }

    private void splitOut(String[] parts,
                          List<Pair<Character, Character>> cars,
                          List<Character> middles,
                          Set<Character> edges) {
        for (int i = 0; i < parts.length; i++) {
            splitOutOne(parts[i], cars, middles, edges);
        }
    }

    private void splitOutOne(String part,
                             List<Pair<Character, Character>> cars,
                             List<Character> middles,
                             Set<Character> edges) {
        char[] chars = part.toCharArray();
        for (int i = 1; i < chars.length - 1; i++) {
            middles.add(chars[i]);
        }
        edges.add(chars[0]);
        edges.add(chars[chars.length-1]);
        cars.add(Pair.of(chars[0], chars[chars.length-1]));
    }

    private String dedup(String str) {
        char prev = '\0';
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c != prev) {
                sb.append(c);
            }
            prev = c;
        }
        return sb.toString();
    }
}
