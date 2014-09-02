package name.arbitrary.y2014.r3;

import com.sun.tools.javac.util.Pair;
import name.arbitrary.CodeJamBase;

import java.util.*;

import static com.google.common.base.MoreObjects.firstNonNull;

// 21:52 - 23:34. 102 minutes, not difficult, just fiddly to implement.
public class B_LastHit extends CodeJamBase {
    B_LastHit(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new B_LastHit(args[0]).run();
    }

    @Override
    protected String runCase() {
        List<Integer> parts = getLineNumericElements();
        assert parts.size() == 3;
        int p = parts.get(0);
        int q = parts.get(1);
        int n = parts.get(2);
        List<Pair<Integer, Integer>> monsters = new ArrayList<Pair<Integer, Integer>>();
        for (int i = 0; i < n; i++) {
            List<Integer> rowParts = getLineNumericElements();
            assert rowParts.size() == 2;
            monsters.add(Pair.of(rowParts.get(0), rowParts.get(1)));
        }
        
        return "" + solve(p, q, monsters);
    }

    // Solution plan is to keep track of maximum score when nth monster dies, given m shots were /not/
    // used on the first n monsters (and thus are available for spending later).
    private int solve(int ourHp, int towerHp, List<Pair<Integer, Integer>> monsters) {
        // Map from unspent shots to max score we could achieve.
        Map<Integer, Integer> leftOverScore = new HashMap<Integer, Integer>();
        // As tower has first attack on next monster after we kill previous one, we can assume that tower always
        // goes first on each monster, and very first monster we get a free hit on.
        leftOverScore.put(1, 0);
        for (Pair<Integer, Integer> monster : monsters) {
            System.err.println("Monster: " + monster);
            int monsterHp = monster.fst;
            int monsterGold = monster.snd;

            Map<Integer, Integer> newLeftOverScore = new HashMap<Integer, Integer>();

            // In worst case, we don't shoot at all, and keep existing score.
            int freeShots = 1 + (monsterHp - 1) / towerHp;
            for (Map.Entry<Integer, Integer> entry : leftOverScore.entrySet()) {
                newLeftOverScore.put(entry.getKey() + freeShots, entry.getValue());
            }

            int maxShots = Collections.max(leftOverScore.keySet());

            for (int i = 0; i <= maxShots; i++) {
                for (int j = 0; j <= monsterHp / towerHp; j++) {
                    // System.err.println("Trying " + monsterHp + " " + i + " " + j);
                    updateWithShots(newLeftOverScore, leftOverScore, ourHp, towerHp, monsterHp, monsterGold, i, j);
                }
            }

            leftOverScore = newLeftOverScore;

            // System.err.println("Finished one monster: " + leftOverScore);
        }

        // Find the highest score...
        return Collections.max(leftOverScore.values());
    }

    // Can we win if we take i shots before the taking-turns phase, and then with j shots in the taking-turns phase?
    // If so, update leftOverScores with the new scores...
    private void updateWithShots(Map<Integer, Integer> newLeftOverScores,
                                 Map<Integer, Integer> oldLeftOverScores,
                                 int ourHp,
                                 int towerHp,
                                 int monsterHp,
                                 int monsterGold,
                                 int i,
                                 int j) {
        monsterHp -= ourHp * i;

        if (j == 0) {
            if (monsterHp <= 0) {
                // Killed already, using up i shots.
                for (Map.Entry<Integer, Integer> score : oldLeftOverScores.entrySet()) {
                    int spareShots = score.getKey();
                    int oldScore = score.getValue();
                    if (spareShots >= i) {
                        tryUpdate(newLeftOverScores, spareShots - i, oldScore + monsterGold);
                    }
                }
            }
        } else {
            // Perform j - 1 shots first, taking turns
            int initialShots = j - 1;
            monsterHp -= (towerHp + ourHp) * initialShots;
            if (monsterHp <= 0) {
                return;
            }

            // How long will it take for the tower to shave off enough points to allow us to win?
            int waitingTurns = monsterHp / towerHp - 1;
            if (waitingTurns < 0) {
                return;
            }

            // Let the tower shave off points...
            monsterHp %= towerHp;

            // Special case
            if (monsterHp == 0 && ourHp >= towerHp) {
                waitingTurns--;
                monsterHp = 1;
            }

            // Can we take the last HPs?
            if (1 <= monsterHp && monsterHp <= ourHp) {
                // Yep!
                for (Map.Entry<Integer, Integer> score : oldLeftOverScores.entrySet()) {
                    int spareShots = score.getKey();
                    int oldScore = score.getValue();
                    if (spareShots >= i) {
                        tryUpdate(newLeftOverScores, spareShots - i + waitingTurns, oldScore + monsterGold);
                    }
                }
            }
        }
    }

    private void tryUpdate(Map<Integer, Integer> newLeftOverScore, int leftOver, int newScore) {
        // System.err.println("Candidate " + leftOver + " " + newScore);
        int oldScore = firstNonNull(newLeftOverScore.get(leftOver), 0);
        if (newScore > oldScore) {
            newLeftOverScore.put(leftOver, newScore);
        }
    }
}
