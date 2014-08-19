package name.arbitrary.y2014.r1c;

import name.arbitrary.CodeJamBase;

import java.util.*;

// 19:48 - 20:11, 23 minutes, while cooking supper
// Some of the time was spent going 'Is it really this easy???'
public class A_PartElf extends CodeJamBase {
    A_PartElf(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new A_PartElf(args[0]).run();
    }

    @Override
    protected String runCase() {
        String[] parts = getLine().split("/");
        long p = Long.parseLong(parts[0]);
        long q = Long.parseLong(parts[1]);

        long gcf = gcf(p, q);
        p /= gcf;
        q /= gcf;

        if (!isPowerOfTwo(q)) {
            return "impossible";
        }

        return "" + (getPowerOfTwo(q) - getPowerOfTwo(p));
    }

    private int getPowerOfTwo(long q) {
        int i = 0;
        while (q > 1) {
            q /= 2;
            i++;
        }
        return i;
    }

    private boolean isPowerOfTwo(long q) {
        return ((q - 1) & q) == 0;
    }

    long gcf(long p, long q) {
        if (q > p) {
            long tmp = p;
            p = q;
            q = tmp;
        }
        while (q != 0) {
            long tmp = p % q;
            p = q;
            q = tmp;
        }
        return p;
    }
}
