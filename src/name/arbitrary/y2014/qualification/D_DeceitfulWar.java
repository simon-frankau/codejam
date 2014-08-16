package name.arbitrary.y2014.qualification;

import name.arbitrary.CodeJamBase;

import java.util.*;

public class D_DeceitfulWar extends CodeJamBase {
    D_DeceitfulWar(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new D_DeceitfulWar(args[0]).run();
    }

    @Override
    protected String runCase() {
        int nItems = Integer.parseInt(getLine());
        SortedSet<Double> naomiBlocks = getBlocks(nItems);
        SortedSet<Double> kenBlocks = getBlocks(nItems);

        int warScore = playWar(naomiBlocks, kenBlocks);

        return "? " + warScore;
    }

    private SortedSet<Double> getBlocks(int nItems) {
        String[] items = getLine().split(" ");
        assert items.length == nItems;
        SortedSet<Double> blocks = new TreeSet<Double>();
        for (String item : items) {
            blocks.add(Double.parseDouble(item));
        }
        return blocks;
    }

    private int playWar(SortedSet<Double> naomiBlocks, SortedSet<Double> kenBlocks) {
        // Ken plays simple greedy strategy (losing when you could win doesn't help him win more later).
        // In face of this, Naomi can't do clever stuff by playing in any particular order.

        TreeSet<Double> kens = new TreeSet<Double>(kenBlocks); // Going to mutate it.
        int score = 0;
        for (double block : naomiBlocks) {
            SortedSet<Double> largerBlocks = kens.tailSet(block);
            if (largerBlocks.isEmpty()) {
                // Ken can't win. Throw away smallest.
                kens.remove(kens.first());
                score++;
            } else {
                // Ken can win using the smallest one that is larger.
                kens.remove(largerBlocks.first());
            }
        }
        return score;
    }
}
