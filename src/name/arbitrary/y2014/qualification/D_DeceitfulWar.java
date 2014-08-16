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
        int deceitfulWarScore = playDeceitfulWar(naomiBlocks, kenBlocks);
        return deceitfulWarScore + " " + warScore;
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

    private int playDeceitfulWar(SortedSet<Double> naomiBlocks, SortedSet<Double> kenBlocks) {
        // If Ken's largest block beats our largest, it will win its round, so eliminate it using our smallest.
        // Do this whenever our largest is not larger than his largest. When ours is largest, win a round!

        // Going to mutate them.
        TreeSet<Double> kens   = new TreeSet<Double>(kenBlocks);
        TreeSet<Double> naomis = new TreeSet<Double>(naomiBlocks);
        int score = 0;
        // Want to iterate backwards, can't be bothered to change comparator, reverse sign, etc.
        while (!kens.isEmpty()) {
            if (kens.last() > naomis.last()) {
                // Sacrifice smallest with a lie.
                kens.remove(kens.last());
                naomis.remove(naomis.first());
            } else {
                // Win!
                kens.remove(kens.last());
                naomis.remove(naomis.last());
                score++;
            }
        }
        return score;
    }
}
