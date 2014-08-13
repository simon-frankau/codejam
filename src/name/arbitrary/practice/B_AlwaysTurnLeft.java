package name.arbitrary.practice;

import com.sun.tools.javac.util.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class B_AlwaysTurnLeft {

    public static void main(String[] args) {
        BufferedReader input = getInput(args[0]);
        int numLines = Integer.parseInt(getString(input));
        for (int i = 0; i < numLines; ++i) {
            B_AlwaysTurnLeft b = new B_AlwaysTurnLeft();
            System.out.println("Case #" + (i+1) + ": " + b.runCase(getString(input)));
        }
    }

    enum Facing { N, E, S, W };

    Facing dir = Facing.S;
    int x = 0;
    int y = 0;

    Facing endDir;

    Map<Pair<Integer, Integer>, Integer> wallMap = new HashMap<Pair<Integer, Integer>, Integer>();

    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;

    private String runCase(String string) {
        String parts[] = string.split(" ");
        String entrance_to_exit = rejig(parts[0]);
        String exit_to_entrance = rejig(parts[1]);

        followPath(entrance_to_exit);
        endDir = dir;
        turnLeft(); turnLeft();
        followPath(exit_to_entrance);
        adjustEdges();

        return map();
    }

    private void adjustEdges() {
        minY++;
        switch (endDir) {
            case N:         break;
            case S: maxY--; break;
            case E: maxX--; break;
            case W: minX++; break;
        }
    }

    // The idea of 'rejig' is to make each step into an explicit 'find leftmost path' step - at each step, we turn
    // left then turn right until the way is clear, so that each right turn will now represent a wall.
    private String rejig(String path) {
        // NB: Replace order is important...
        path = path.replaceAll("LW", "lw");
        path = path.replaceAll("RRW", "lrrrw");
        path = path.replaceAll("RW", "lrrw");
        path = path.replaceAll("W", "lrw");
        return path;
    }

    private void followPath(String path) {
        for (char c : path.toCharArray()) {
            switch (c) {
                case 'l':
                    turnLeft();
                    break;
                case 'r':
                    markWall();
                    turnLeft(); turnLeft(); turnLeft();
                    break;
                case 'w':
                    forward();
                    break;
                default:
                    throw new RuntimeException("Bad input:" + c);
            }

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
    }

    private void forward() {
        switch (dir) {
            case N: y--; break;
            case E: x++; break;
            case S: y++; break;
            case W: x--; break;
        }
    }

    private void turnLeft() {
        switch (dir) {
            case N: dir = Facing.W; break;
            case E: dir = Facing.N; break;
            case S: dir = Facing.E; break;
            case W: dir = Facing.S; break;
        }
    }

    private void markWall() {
        Pair coord = Pair.of(x,y);
        Integer wallVal = wallMap.get(coord);
        if (wallVal == null) {
            wallVal = 0;
        }
        switch (dir) {
            case N: wallVal |= 1; break;
            case S: wallVal |= 2; break;
            case W: wallVal |= 4; break;
            case E: wallVal |= 8; break;
        }
        wallMap.put(coord, wallVal);
    }

    private static final char[] chars = "0123456789abcdef".toCharArray();

    private char wallAt(int x, int y) {
        Integer wallVal = wallMap.get(Pair.of(x, y));
        if (wallVal == null) {
            wallVal = 0;
        }
        return chars[wallVal ^ 0xf];
    }

    private String map() {
        StringBuffer sb = new StringBuffer();
        for (int y = minY; y <= maxY; y++) {
            sb.append('\n');
            for (int x = minX; x <= maxX; x++) {
                sb.append(wallAt(x, y));
            }
        }
        return sb.toString();
    }

    private static String getString(BufferedReader input) {
        try {
            return input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return "CAN'T HAPPEN";
    }

    private static BufferedReader getInput(String fileName) {
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return input;
    }
}
