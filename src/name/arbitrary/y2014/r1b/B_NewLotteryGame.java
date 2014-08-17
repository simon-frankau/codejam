package name.arbitrary.y2014.r1b;

import name.arbitrary.CodeJamBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 14:49 - 15:00, 15:28 - 15:37 (SimpleCount solved small - 20 minutes)
// 15:37 - 16:00, 19:36 - 20:32, 20:35 - 20:46 (large solved - 90 minutes extra - what a time sink!)
// For large case, tedious problems included:
// * Forgetting to use long for the final count
// * Working out how best to expand the globs
// * Bug hunting in the 'and' and 'compare' logic
public class B_NewLotteryGame extends CodeJamBase {
    B_NewLotteryGame(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new B_NewLotteryGame(args[0]).run();
    }

    protected String runCase() {
        String[] parts = getLine().split(" ");
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        int k = Integer.parseInt(parts[2]);

        List<List<Character>> a2 = toBits(a);
        System.err.println("A: " + a2);
        List<List<Character>> b2 = toBits(b);
        System.err.println("B: " + b2);
        List<List<Character>> k2 = toBits(k);
        System.err.println("K: " + k2);

        List<List<Character>> c = andify(a2, b2);
        System.err.println("ANDed: " + c);

        long i = countMatches(c, k2);
/*
        if (simpleCount(a, b, k) != i) {
            throw new RuntimeException("FAIL! " + a + ", " + b + ", " + k);
        }
*/
        return "" + i;
    }

    private long countMatches(List<List<Character>> vals, List<List<Character>> tgts) {
        long i = 0;
        for (List<Character> val : vals) {
            for (List<Character> tgt : tgts) {
                long match = countMatch(val, tgt);
                i += match;
            }
        }
        return i;
    }

    private long countMatch(List<Character> val, List<Character> tgt) {
        long total = 1;
        for (int i = 0; i < val.size(); i++) {
            total *= countOne(val.get(i), tgt.get(i));
            if (total == 0) {
                return 0;
            }
        }
        return total;
    }

    private long countOne(Character val, Character tgt) {
        switch (val) {
            case '0':
                switch (tgt) {
                    case '0': return 1;
                    case '1': return 0;
                    case 'X': return 1;
                }
            case '1':
                switch (tgt) {
                    case '0': return 0;
                    case '1': return 1;
                    case 'X': return 1;
                }
            case 'A':
                switch (tgt) {
                    case '0': return 2;
                    case '1': return 0;
                    case 'X': return 2;
                }
            case 'B':
                switch (tgt) {
                    case '0': return 0;
                    case '1': return 2;
                    case 'X': return 2;
                }
            case 'X':
                switch (tgt) {
                    case '0': return 1;
                    case '1': return 1;
                    case 'X': return 2;
                }
            case 'Y':
                switch (tgt) {
                    case '0': return 3;
                    case '1': return 1;
                    case 'X': return 4;
                }
        }
        throw new RuntimeException("Can't happen");
    }

    private List<List<Character>> andify(List<List<Character>> a, List<List<Character>> b) {
        List<List<Character>> result = new ArrayList<List<Character>>();
        for (List<Character> charactersA : a) {
            for (List<Character> charactersB : b) {
                result.add(andifyOne(charactersA, charactersB));
            }
        }
        return result;
    }

    private List<Character> andifyOne(List<Character> charactersA, List<Character> charactersB) {
        List<Character> result = new ArrayList<Character>();
        for (int i = 0; i < charactersA.size(); ++i) {
            char a = charactersA.get(i);
            char b = charactersB.get(i);
            char c = 'Q';
            switch (a) {
                case '0':
                    switch (b) {
                        case '0': c = '0'; break;
                        case '1': c = '0'; break;
                        case 'X': c = 'A'; break; // 'A' means 0, twice.
                    }
                    break;
                case '1':
                    switch (b) {
                        case '0': c = '0'; break;
                        case '1': c = '1'; break;
                        case 'X': c = 'X'; break;
                    }
                    break;
                default:
                    switch (b) {
                        case '0': c = 'A'; break;
                        case '1': c = 'X'; break;
                        case 'X': c = 'Y'; break; // 'Y' means '0' x 3, '1' x 1
                    }
            }
            result.add(c);
        }
        return result;
    }

    List<List<Character>> toBits(int i) {
        return expandBit(getBitStr(i));
    }

    // Useful for checking.
    public long simpleCount(int a, int b, int k) {
        long count = 0;
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++) {
                 if ((i & j) < k) {
                     count++;
                 }
            }
        }
        return count;
    }

    // Convert into an explicit bit string...
    public List<Character> getBitStr(int i) {
        List<Character> bits = new ArrayList<Character>();
        for (int j = 0; j < 32; j++) {
            bits.add((i & 1) != 0 ? '1' : '0');
            i /= 2;
        }
        Collections.reverse(bits);
        return bits;
    }

    // Convert a bit string into a set of globs
    public List<List<Character>> expandBit(List<Character> bitStr) {
        List<Character> bitStrCopy = new ArrayList<Character>(bitStr);
        List<List<Character>> result = new ArrayList<List<Character>>();
        for (int i = 0; i < bitStrCopy.size(); ++i) {
            if (bitStrCopy.get(i) != '0') {
                List<Character> newItem = new ArrayList<Character>(bitStrCopy);
                newItem.set(i, '0');
                for (int j = i + 1; j < newItem.size(); ++j) {
                    newItem.set(j, 'X');
                }
                result.add(newItem);
            }
        }
        return result;
    }
}
