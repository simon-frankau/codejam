package name.arbitrary.practice;

import com.sun.tools.javac.util.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
        private final List<Pair<Integer, Double>> prices;
        Store(double x, double y, List<Pair<Integer, Double>> prices) {
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
            for (Pair<Integer, Double> entry : prices) {
                sb.append(revShoppingList.get(entry.fst)).append(':').append(entry.snd).append(' ');
            }
            return sb.toString();
        }
    };

    // Items will be represented with a bit mask. This holds the mapping.
    Map<String, Integer> shoppingList = new HashMap<String, Integer>();
    Map<Integer, String> revShoppingList = new HashMap<Integer, String>();

    // Bitmask for perishable items.
    int perishables;
    // And target
    int target;

    double priceOfGas;
    Set<Store> stores = new HashSet<Store>();

    // NB: Null Store equals home.
    class State implements Comparable<State> {
        State(Store location, int basket, double spent) {
            this.location = location;
            this.basket = basket;
            this.spent = spent;
        }
        public Store location;
        public int basket;
        public double spent;

        @Override
        public String toString() {
            return basket + " " + spent + " " + location;
        }

        @Override
        public int compareTo(State state) {
            return Double.compare(spent, state.spent);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            State state = (State) o;

            if (basket != state.basket) return false;
            if (Double.compare(state.spent, spent) != 0) return false;
            if (location != null ? !location.equals(state.location) : state.location != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = location != null ? location.hashCode() : 0;
            result = 31 * result + basket;
            temp = Double.doubleToLongBits(spent);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    PriorityQueue<State> toProcess = new PriorityQueue<State>();
    Map<State, State> queuedLookup = new HashMap<State, State>();
    Set<State> processed = new HashSet<State>();

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

        solve();

        return dumpState();
    }

    private State solve() {
        toProcess.add(new State(null, 0, 0.0));

        while (!toProcess.isEmpty()) {
            State next = toProcess.remove();
            System.out.println("Running " + next);
            if (next.location == null && next.basket == target) {
                return next;
            }
            State zeroedState = new State(next.location, next.basket, 0.0);
            processed.add(zeroedState);
            enqueueNextSteps(next);
            queuedLookup.remove(zeroedState);
        }

        return null;
    }

    private void enqueueState(State state) {
        System.out.println("Enqueuing " + state);
        State zeroedState = new State(state.location, state.basket, 0.0);
        if (processed.contains(zeroedState)) {
            System.out.println("Already processed");
            return;
        }
        State existing = queuedLookup.get(zeroedState);
        if (existing != null) {
            if (existing.spent > state.spent) {
                System.out.println("Better route " + existing.spent + " vs. " + state.spent);
                toProcess.remove(existing);
                queuedLookup.remove(zeroedState);
                toProcess.add(state);
                queuedLookup.put(zeroedState, state);
            } else {
                System.out.println("Worse route");
            }
        } else {
            System.out.println("New state");
            toProcess.add(state);
            queuedLookup.put(zeroedState, state);
        }
    }

    private State goTo(State state, Store location) {
        double gasPrice = state.location == null ?
                (location == null ? 0.0 : location.costTo(state.location)) :
                state.location.costTo(location);
        return new State(location, state.basket, state.spent + gasPrice);
    }

    private void enqueueNextSteps(State state) {
        List<State> shopped = doShopping(state);

        if (state.location != null) {
            for (State newState : shopped) {
                enqueueState(goTo(newState, null));
            }
        }

        for (State newState : shopped) {
            int boughtItems = newState.basket - state.basket;
            boolean havePerishables = (boughtItems & perishables) != 0;
            if (!havePerishables) {
                for (Store store : stores) {
                    if (store != state.location) {
                        enqueueState(goTo(newState, store));
                    }
                }
            }
        }
    }

    private List<State> doShopping(State state) {
        List<State> result = Collections.singletonList(state);

        // No shopping at home!
        if (state.location == null) {
            return result;
        }

        for (Pair<Integer, Double> price : state.location.prices) {
            if ((state.basket & price.fst) == 0) {
                // Not bought yet.
                List<State> newResult = new ArrayList<State>(result.size() * 2);
                for (State s : result) {
                    newResult.add(s);
                    newResult.add(new State(s.location, s.basket | price.fst, s.spent + price.snd));
                }
                result = newResult;
            }
        }

        return result;
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
        target = (1 << items.length) - 1;
    }

    private void addStore(String string) {
        String parts[] = string.split(" ");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        int mask = 0;
        List<Pair<Integer, Double>> prices = new ArrayList<Pair<Integer, Double>>();
        for (int i = 2; i < parts.length; i++) {
            String bits[] = parts[i].split(":");
            prices.add(Pair.of(shoppingList.get(bits[0]), Double.parseDouble(bits[1])));
        }
        stores.add(new Store(x, y, prices));
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
