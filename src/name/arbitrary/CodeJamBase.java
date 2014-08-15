package name.arbitrary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class with basic framework for doing an answer...
 */
public abstract class CodeJamBase {
    private final BufferedReader input;

    public CodeJamBase(String fileName) {
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.input = input;
    }

    public void run() {
        int numLines = Integer.parseInt(getLine());
        for (int i = 0; i < numLines; ++i) {
            System.out.println("Case #" + (i+1) + ": " + runCase());
        }
    }

    protected abstract String runCase();

    protected String getLine() {
        try {
            return input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return "CAN'T HAPPEN";
    }
}
