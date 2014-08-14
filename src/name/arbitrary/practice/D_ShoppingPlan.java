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
        private final double x;
        private final double y;
        private final Map<Integer, Double> prices;
        Store(double x, double y, Map<Integer, Double> prices) {
            this.x = x;
            this.y = y;
            this.prices = prices;
        }

        double costTo(Store store) {
            double x2 = store != null ? store.x : 0.0;
            double y2 = store != null ? store.y : 0.0;
            return priceOfGas * Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(x).append(' ').append(y).append(' ');
            for (Map.Entry<Integer, Double> entry : prices.entrySet()) {
                sb.append(revShoppingList.get(entry.getKey())).append(':').append(entry.getValue()).append(' ');
            }
            return sb.toString();
        }
    };

    // Items will be represented with a bit mask. This holds the mapping.
    Map<String, Integer> shoppingList = new HashMap<String, Integer>();
    Map<Integer, String> revShoppingList = new HashMap<Integer, String>();

    // Bitmask for perishable items.
    int perishables;

    double priceOfGas;
    Set<Store> stores = new HashSet<Store>();

    // TODO: General plan is dynamic programming over the things we have and which store/home we're at.
    private String runCase(BufferedReader input) {
        String parts[] = getString(input).split(" ");
        int numItems = Integer.parseInt(parts[0]);
        int numStores = Integer.parseInt(parts[1]);
        priceOfGas = Double.parseDouble(parts[2]);

        String items[] = getString(input).split(" ");
        assert (items.length == numItems);
        constructShoppingList(items);

        for (int i = 0; i < numStores; ++i) {
            addStore(getString(input));
        }

        return dumpState();
    }

    private String dumpState() {
        StringBuffer sb = new StringBuffer();
        sb.append(shoppingList.size()).append(' ').append(stores.size()).append(' ').append(priceOfGas).append('\n');
        for (Map.Entry<String, Integer> entry : shoppingList.entrySet()) {
            sb.append(entry.getKey());
            if ((entry.getValue() & perishables) != 0) {
                sb.append('!');
            }
            sb.append(' ');
        }
        sb.append('\n');
        for (Store store : stores) {
            sb.append(store.toString()).append('\n');
        }
        return sb.toString();
    }

    private void constructShoppingList(String[] items) {
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            boolean isPerishable = item.endsWith("!");
            if (isPerishable) {
                item = item.substring(0, item.length() - 1);
            }
            int bit = 1 << i;
            shoppingList.put(item, bit);
            revShoppingList.put(bit, item);
            if (isPerishable) {
                perishables |= bit;
            }
        }
    }

    private void addStore(String string) {
        String parts[] = string.split(" ");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        int mask = 0;
        Map<Integer, Double> prices = new HashMap<Integer, Double>();
        for (int i = 2; i < parts.length; i++) {
            String bits[] = parts[i].split(":");
            prices.put(shoppingList.get(bits[0]), Double.parseDouble(bits[1]));
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
