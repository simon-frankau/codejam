package name.arbitrary.y2014.r3;

import name.arbitrary.CodeJamBase;

import java.util.List;

// Initially 22:15-22:57
// Fix bug 07:56-8:12
// 58 minutes - really rather slower than it should be!
public class A_MagicalMarvellousTour extends CodeJamBase {
    A_MagicalMarvellousTour(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new A_MagicalMarvellousTour(args[0]).run();
    }

    @Override
    protected String runCase() {
        List<Integer> parts = getLineNumericElements();
        int  n = parts.get(0);
        long p = parts.get(1);
        long q = parts.get(2);
        long r = parts.get(3);
        long s = parts.get(4);

        long[] transistors = getArray(n, p, q, r, s);

        return String.format("%.10f", solve(transistors));
    }

    private double solve(long[] transistors) {
        // Given end-points a and b, want to minimise the worst case for the three segments.
        // i.e. find the point at which we have 1/3rd in each, pretty much.

        // Due to corner cases (*), we try each left hand end, and then get the appropriate right-hand
        // end that minimises the probability of the opponent winning.

        // (*) If the first segment is a bit more than a third, the other two segments are less than a third,
        // and they should be split evenly, rather than targeting the last segment to be a third.

        long total = 0;
        for (long i : transistors) {
            total += i;
        }

        double minScore = 1.0;

        // Try each left end
        long lhsTotal = 0L;    // Sum less than lhsIdx
        long rhsTotal = total; // Sum greater than or equal to rhsIdx
        int rhsIdx = 0;
        for (int lhsIdx = 0; lhsIdx < transistors.length; lhsIdx++) {
            long remaining = total - lhsTotal;
            long target = remaining / 2;
            while (rhsIdx < transistors.length && rhsTotal - transistors[rhsIdx] > target) {
                rhsTotal -= transistors[rhsIdx];
                rhsIdx++;
            }

            minScore = Math.min(minScore, probCase(total, lhsTotal, rhsTotal));

            if (rhsIdx < transistors.length) {
                minScore = Math.min(minScore, probCase(total, lhsTotal, rhsTotal - transistors[rhsIdx]));
            }

            lhsTotal += transistors[lhsIdx];
        }

        return 1.0 - minScore;
    }

    private double probCase(long total, long lhsTotal, long rhsTotal) {
        return Math.max(total - lhsTotal - rhsTotal,
                Math.max(lhsTotal, rhsTotal)) / (double)total;
    }

    private long[] getArray(int n, long p, long q, long r, long s) {
        long[] result = new long[n];
        for (int i = 0; i < n; i++) {
            result[i] = (i * p + q) % r + s;
        }
        return result;
    }
}