package name.arbitrary.y2014.r1b;

import name.arbitrary.CodeJamBase;

import java.util.*;

// 7:52 - 8:03, 08:22 - 8:33. Total time: 22 minutes
public class A_TheRepeater extends CodeJamBase {
    A_TheRepeater(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new A_TheRepeater(args[0]).run();
    }

    @Override
    protected String runCase() {
        int n = Integer.parseInt(getLine());
        List<String> strings = new ArrayList<String>(n);
        for (int i = 0; i < n; i++) {
            strings.add(getLine());
        }

        if (!checkCanDo(strings)) {
            return "Fegla Won";
        }
        List<List<Integer>> nums = toNums(strings);

        int cost = 0;
        for (int i = 0; i < nums.get(0).size(); i++) {
            cost += costOf(i, nums);
        }
        System.err.println("Total " + cost);
        return "" + cost;
    }

    private int costOf(int i, List<List<Integer>> nums) {
        List<Integer> nths = new ArrayList<Integer>(nums.size());
        for (List<Integer> item : nums) {
            nths.add(item.get(i));
        }
        Collections.sort(nths);
        // Optimal item is median.
        int optimal = nths.get(nths.size() / 2);
        int cost = 0;
        for (int j : nths) {
            cost += Math.abs(j - optimal);
        }
        System.err.println("Adding "+ cost);
        return cost;
    }

    private List<List<Integer>> toNums(List<String> strings) {
        List<List<Integer>> result = new ArrayList<List<Integer>>(strings.size());
        for (String string : strings) {
            int i = 0;
            List<Integer> item = new ArrayList<Integer>();
            char last = '\n';
            for (char c : string.toCharArray()) {
                if (c != last) {
                    if (i != 0) {
                        item.add(i);
                    }
                    i = 1;
                } else {
                    i++;
                }
                last = c;
            }
            item.add(i);
            result.add(item);
        }
        System.err.println(result);
        return result;
    }

    private boolean checkCanDo(List<String> strings) {
        Set<List<Character>> collapsedStrings = new HashSet<List<Character>>();
        for (String string : strings) {
            List<Character> dedup = new ArrayList<Character>();
            char[] cs = string.toCharArray();
            char last = '\0';
            for (int i = 0; i < cs.length; i++) {
                char c = cs[i];
                if (c != last) {
                    dedup.add(c);
                }
                last = c;
            }
            collapsedStrings.add(dedup);
        }
        return collapsedStrings.size() == 1;
    }
}