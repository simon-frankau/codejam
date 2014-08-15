package name.arbitrary.y2014.qualification;

import name.arbitrary.CodeJamBase;

public class C_MinesweeperMaster extends CodeJamBase {
    public C_MinesweeperMaster(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new C_MinesweeperMaster(args[0]).run();
    }

    class ImpossibleException extends Exception {
    }

    @Override
    protected String runCase() {
        String[] parts = getLine().split(" ");
        int r = Integer.parseInt(parts[0]);
        int c = Integer.parseInt(parts[1]);
        int m = Integer.parseInt(parts[2]);

        // At least 4 columns, 3 rows:
        //
        // ***********
        // *********..
        // *********..
        //
        // ***********
        // ********...
        // ********...
        //
        // *********..
        // ********...
        // ********...
        //
        // ********...
        // ********...
        // ********...
        //
        // *********..
        // *******....
        // *******....
        //
        // ********...
        // *******....
        // *******....
        //
        // ********...
        // ******.....
        // ******.....
        // etc.
        //
        // i.e., 8 or more with >= 3 rows and sufficiently (?) wide is soluble.

        // 2 columns have to be even number.
        // 1 column is obvious (?)

        boolean transpose = r > c;
        if (transpose) {
            int tmp = r;
            r = c;
            c = tmp;
        }

        char[][] map = new char[r][c];

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                map[i][j] = '*';
            }
        }

        int toClear = r * c - m;

        try {
            switch (r) {
                case 1:
                    solve1(map, toClear);
                    break;
                case 2:
                    solve2(map, toClear);
                    break;
                default:
                    solveN(map, toClear);
                    break;
            }
            // TODO! Actually carve out the appropriate number, throwing exception that we should catch if you can't.
        } catch (ImpossibleException e) {
            return "\nImpossible";
        }

        // Start point!
        map[0][0] = 'c';

        if (transpose) {
            return printTransposed(map);
        } else {
            return printNormal(map);
        }
    }

    private void solve1(char[][] map, int toClear) throws ImpossibleException {
        if (toClear <= 0) {
            throw new ImpossibleException();
        }

        for (int i = 0; i < toClear; i++) {
            map[0][i] = '.';
        }
    }

    private void solve2(char[][] map, int toClear) throws ImpossibleException {
        if (toClear <= 0) {
            throw new ImpossibleException();
        }

        if (toClear == 1) {
            return;
        }

        if (toClear % 2 != 0) {
            throw new ImpossibleException();
        }

        for (int i = 0; i < toClear/2; i++) {
            map[0][i] = '.';
            map[1][i] = '.';
        }
    }

    private void solveN(char[][] map, int toClear) throws ImpossibleException {
        if (toClear <= 0) {
            throw new ImpossibleException();
        }

        if (toClear == 1) {
            return;
        }

        // TODO: All the other cases!
    }


    private String printNormal(char[][] map) {
        StringBuilder sb = new StringBuilder();
        int r = map.length;
        int c = map[0].length;
        for (int i = 0; i < r; i++) {
            sb.append('\n');
            for (int j = 0; j < c; j++) {
                sb.append(map[i][j]);
            }
        }
        return sb.toString();
    }

    private String printTransposed(char[][] map) {
        StringBuilder sb = new StringBuilder();
        int r = map.length;
        int c = map[0].length;
        for (int i = 0; i < c; i++) {
            sb.append('\n');
            for (int j = 0; j < r; j++) {
                sb.append(map[j][i]);
            }
        }
        return sb.toString();
    }
}
