package name.arbitrary.practice;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class D_ShoppingPlan {

    public static void main(String[] args) {
        BufferedReader input = getInput(args[0]);
        int numLines = Integer.parseInt(getString(input));
        for (int i = 0; i < numLines; ++i) {
            D_ShoppingPlan test = new D_ShoppingPlan();
            System.out.println("Case #" + (i+1) + ": " + test.runCase(input));
        }
    }

    class Store {
        Store(double x, double y, Map<String, Double> prices) {
            // TODO!
        }
    };

    double priceOfGas;
    Set<String> shoppingList = new HashSet<String>(); // TODO: Make map to perishable boolean
    Set<Store> stores = new HashSet<Store>();

    // TODO: General plan is dynamic programming over the things we have and which store/home we're at.
    private String runCase(BufferedReader input) {
        String parts[] = getString(input).split(" ");
        int numItems = Integer.parseInt(parts[0]);
        int numStores = Integer.parseInt(parts[1]);
        priceOfGas = Double.parseDouble(parts[2]);

        String items[] = getString(input).split(" ");
        assert (items.length == numItems);
        for (String item : items) {
            shoppingList.add(item);
        }

        for (int i = 0; i < numStores; ++i) {
            addStore(getString(input));
        }

        return "FIXME";
    }

    private void addStore(String string) {
        String parts[] = string.split(" ");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        Map<String, Double> prices = new HashMap<String, Double>();
        for (int i = 2; i < parts.length; i++) {
            String bits[] = string.split(":");
            prices.put(bits[0], Double.parseDouble(bits[1]));
        }
        stores.add(new Store(x, y, prices)); // TODO
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
