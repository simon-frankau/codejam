package name.arbitrary.practice;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class A_AlienNumbers {

    public static void main(String[] args) {
        BufferedReader input = getInput(args[0]);
        int numLines = Integer.parseInt(getString(input));
        for (int i = 0; i < numLines; ++i) {
            System.out.println("Case #" + (i+1) + ": " + runCase(getString(input)));
        }
    }

    private static String runCase(String string) {
        String parts[] = string.split(" ");
        String numStr = parts[0];
        String source = parts[1];
        String target = parts[2];

        Map<Character, Integer> srcMap = new HashMap<Character, Integer>();
        int i = 0;
        for (char c : source.toCharArray()) {
            srcMap.put(c, i++);
        }

        long num = 0;
        for (char c : numStr.toCharArray()) {
            num *= source.length();
            num += srcMap.get(c);
        }

        char[] tgtMap = target.toCharArray();

        if (num == 0) {
            return "" + tgtMap[0];
        }

        StringBuilder result = new StringBuilder();
        while (num > 0) {
            result.append(tgtMap[(int) (num % target.length())]);
            num /= target.length();
        }

        return result.reverse().toString();
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
