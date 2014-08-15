package name.arbitrary.y2014.qualification;

import name.arbitrary.CodeJamBase;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sgf on 15/08/2014.
 */
public class A_MagicTrick extends CodeJamBase{
    private final static int NUM_LINES = 4;
    A_MagicTrick(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new A_MagicTrick(args[0]).run();
    }

    Set<String> getCandidates() {
        int lineNum = Integer.parseInt(getLine());
        Set<String> numberSet = new HashSet<String>();
        for (int i = 1; i <= NUM_LINES; i++) {
            String line = getLine();
            if (i == lineNum) {
                String[] numbers = line.split(" ");
                for (String number : numbers) {
                    numberSet.add(number);
                }
            }
        }
        return numberSet;
    }

    @Override
    protected String runCase() {
        Set<String> set = getCandidates();
        set.retainAll(getCandidates());
        switch (set.size()) {
            case 0:
                return "Volunteer cheated!";
            case 1:
                return set.iterator().next();
            default:
                return "Bad magician!";
        }
    }
}
