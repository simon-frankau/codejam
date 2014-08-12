package name.arbitrary;

import java.awt.image.BufferedImage;
import java.io.*;

public class Main {

    public static void main(String[] args) {
        BufferedReader input = getInput(args[0]);
        int numLines = Integer.parseInt(getString(input));
        for (int i = 0; i < numLines; ++i) {
            System.out.println(runCase(getString(input)));
        }
    }

    private static String runCase(String string) {
        String parts[] = string.split(" ");
        StringBuffer sb = new StringBuffer();
        for (String part : parts) {
            sb.append(part).append("-");
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
