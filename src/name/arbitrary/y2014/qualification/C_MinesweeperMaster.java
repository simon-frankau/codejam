package name.arbitrary.y2014.qualification;

import name.arbitrary.CodeJamBase;

/**
 * Created by sgf on 15/08/2014.
 */
public class C_MinesweeperMaster extends CodeJamBase {
    public C_MinesweeperMaster(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new C_MinesweeperMaster(args[0]).run();
    }

    @Override
    protected String runCase() {
        String[] parts = getLine().split(" ");
        int r = Integer.parseInt(parts[0]);
        int c = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[0]);

        int space = r * c;

        // Basic case: At least 4 columns, 2 empty rows.
        //
        // Can solve a shape that looks like:
        // ***********
        // ***********
        // *********..
        // ...........
        // ...........
        // ...........
        // That is, at least 2 blank lines and 0 or at least 2 cells left blank on the first filled line...
        //
        // Only missing case is if you want one item missing from the last row.
        //
        // In that case, we do:
        // ************
        // ************
        // *********...
        // *...........
        // *...........

        // Now try: At least 4 columns, not a full empty row...
        // ********
        // *****...
        // ***.....
        // ***.....
    }
}
