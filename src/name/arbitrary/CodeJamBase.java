package name.arbitrary;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

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
            System.err.println("Case #" + (i+1));
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

    protected String[] getLineElements() {
        return getLine().split(" ");
    }

    protected List<Integer> getLineNumericElements() {
        List<Integer> result = Lists.newArrayList();
        for (String element : getLineElements()) {
            result.add(Integer.parseInt(element));
        }
        return result;
    }

    protected List<String> getLines(int n) {
        List<String> result = Lists.newArrayListWithCapacity(n);
        for (int i = 0; i < n; i++) {
             result.add(getLine());
        }
        return result;
    }

    protected List<Integer> getNumericLines(int n) {
        List<Integer> result = Lists.newArrayListWithCapacity(n);
        for (int i = 0; i < n; i++) {
            result.add(Integer.parseInt(getLine()));
        }
        return result;
    }
}
