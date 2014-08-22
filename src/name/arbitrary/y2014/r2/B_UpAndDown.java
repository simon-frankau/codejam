package name.arbitrary.y2014.r2;

import name.arbitrary.CodeJamBase;

import java.util.ArrayList;
import java.util.List;

// Inefficient implementation, as it's quick to do and solves the given problem fast enough
public class B_UpAndDown extends CodeJamBase {
    B_UpAndDown(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new B_UpAndDown(args[0]).run();
    }

    @Override
    protected String runCase() {
        int n = Integer.parseInt(getLine());
        String[] numStrs = getLine().split(" ");
        assert n == numStrs.length;
        List<Integer> nums = new ArrayList<Integer>();
        for (String numStr : numStrs) {
            nums.add(Integer.parseInt(numStr));
        }

        // We push the smallest numbers to the edges. Push them to the nearest edge and repeat.
        int totalCost = 0;
        while (!nums.isEmpty()) {
            totalCost += moveMinimum(nums);
        }

        return "" + totalCost;
    }

    private int moveMinimum(List<Integer> nums) {
        int len = nums.size();
        int smallest = Integer.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < len; i++) {
            int currVal = nums.get(i);
            if (currVal < smallest) {
                smallest = currVal;
                idx = i;
            }
        }

        // Equivalent to moving it to the end, and then removing...
        nums.remove(idx);
        // Find the cost of moving it to the nearest end.
        return Math.min(idx, len - 1 - idx);
    }
}