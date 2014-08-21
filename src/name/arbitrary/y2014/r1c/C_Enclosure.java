package name.arbitrary.y2014.r1c;

import name.arbitrary.CodeJamBase;

public class C_Enclosure extends CodeJamBase {
    C_Enclosure(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new C_Enclosure(args[0]).run();
    }

    @Override
    protected String runCase() {
        String[] parts = getLine().split(" ");
        int n = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int k = Integer.parseInt(parts[2]);

        return "" + process(n, m, k);
    }

// Given enough space, the most efficient shape will be diamond-like.

// x
//
// xx
//
// xx
// x
//
// x
// xx
// x
//
//  x
// x.x   0
//  x
//
//  xx
// x.x   1
//  x
//
//  xx
// x..x  2
//  xx
//
//  xx
// x..x  3
//  x.x
//   x
//
//   x
//  x.x
// x...x 0
//  x.x
//   x

    private int process(int n, int m, int k) {
        int thinner = Math.min(n, m);
        int fatter = Math.max(n, m);

        int width = 1;
        int height = 0;
        int area = 0;
        int stones = 0;

        int state = 0;
        int phase = 0;

        boolean reachedEdge = false;

        while (!reachedEdge) {
            switch (state) {
                case 0:
                    if (height == fatter) {
                        reachedEdge = true;
                        break;
                    }
                    area += Math.max(phase, 1);
                    if (phase == 0) {
                        height++;
                    }
                    state++;
                    stones++;
                    break;
                case 1:
                    area += phase + 1;
                    if (width != thinner) {
                        width++;
                        state++;
                    } else {
                        height++;
                        state = 0;
                    }
                    stones++;
                    break;
                case 2:
                    if (height == fatter) {
                        reachedEdge = true;
                        break;
                    }
                    height++;
                    area += phase + 1;
                    state++;
                    stones++;
                    break;
                case 3:
                    if (width != thinner) {
                        width++;
                        height++;
                        area += phase + 2;
                        phase++;
                        state = 0;
                    } else {
                        area += phase + 1;
                        state = 2;
                    }
                    stones++;
                    break;
            }
            if (!reachedEdge) {
                if (area >= k) {
                    System.err.println(stones);
                    return stones;
                }
                System.err.println(stones + " -> " + area + " (" + width + ", " + height + ", " + state + ")");
            }
        }

        // At this point, we have filled in the inside of the rectangle with an extended diamond...
        // just the corners remain.
        System.err.println("Reached edge");
        long edgePart = ((thinner + 1)/ 2) - 1;
        while (area < n * m) {
            for (int i = 0; i < 4; i++) {
                area += edgePart;
                stones++;
                if (area >= k) {
                    System.err.println(stones);
                    return stones;
                }
                System.err.println(stones + " -> " + area + " (" + width + ", " + height + ")");
            }
            edgePart--;
        }

        return stones;
    }
}