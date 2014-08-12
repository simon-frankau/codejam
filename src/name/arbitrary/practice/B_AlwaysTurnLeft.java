package name.arbitrary.practice;

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

    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;

    private String runCase(String string) {
        String parts[] = string.split(" ");
        String entrance_to_exit = parts[0];
        String exit_to_entrance = parts[1];

        followPath(entrance_to_exit);
        turnLeft(); turnLeft();
        followPath(exit_to_entrance);

        return "FIXME";
    }

    private void followPath(String path) {
        for (char c : path.toCharArray()) {
            switch (c) {
                case 'L':
                    turnLeft();
                    break;
                case 'R':
                    turnLeft(); turnLeft(); turnLeft();
                    break;
                case 'W':
                    forward();
                    break;
                default:
                    throw new RuntimeException("Bad input:" + c);
            }

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);

            System.out.println(x + ", " + y);
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
