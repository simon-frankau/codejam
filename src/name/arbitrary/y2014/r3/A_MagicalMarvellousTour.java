package name.arbitrary.y2014.r3;

import name.arbitrary.CodeJamBase;

import java.util.List;

// Started 22:15.
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

        long total = 0;
        for (long i : transistors) {
            total += i;
        }

        long target = total / 3;

        // Generate last index to the left of which we're below 1/3, starting from left.
        int lhsIdx = 0;
        long lhsTotal = 0;
        while (lhsTotal + transistors[lhsIdx] < target) {
            lhsTotal += transistors[lhsIdx];
            lhsIdx++;
        }

        // Similar, from the right.
        int rhsIdx = transistors.length - 1;
        long rhsTotal = 0;
        while (rhsTotal + transistors[rhsIdx] < target) {
            rhsTotal += transistors[rhsIdx];
            rhsIdx--;
        }

        System.err.println(lhsIdx + " " + rhsIdx);

        double probWin = probCase(total, lhsTotal, rhsTotal);
        if (lhsIdx < rhsIdx) {
            double probWin2 = probCase(total, lhsTotal + transistors[lhsIdx], rhsTotal);
            double probWin3 = probCase(total, lhsTotal, rhsTotal + transistors[rhsIdx]);
            probWin = Math.min(probWin, Math.min(probWin2, probWin3));
        }
        if (lhsIdx + 1 < rhsIdx) {
            double probWin4 = probCase(total, lhsTotal + transistors[lhsIdx], rhsTotal + transistors[rhsIdx]);
            probWin = Math.min(probWin, probWin4);
        }
        return 1.0 - probWin;
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