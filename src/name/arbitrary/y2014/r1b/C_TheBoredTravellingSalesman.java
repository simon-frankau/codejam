package name.arbitrary.y2014.r1b;

import name.arbitrary.CodeJamBase;

import java.util.*;

// 21:05 - 21:13, 21:41 - 23:03. Argh. Giving up timing. Too hard to think about with time pressure. :p
// 00:23 - Cracked it, completed large. About 2 hours 45 minutes total. Aaaaaaaaaaaargh.
public class C_TheBoredTravellingSalesman extends CodeJamBase {
    C_TheBoredTravellingSalesman(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new C_TheBoredTravellingSalesman(args[0]).run();
    }

    // Basically it's finding a tree traversal that covers all the nodes and maximises the score.

    // At each step, we have the choice of going into the next-size-sized node, or returning to our parent and carrying
    // on there. Except, of course, if going to the parent with traversing more children leaves some nodes unreachable.
    // So, we generate a set of next possible moves that we can make, of taking the next sibling at each level, up to
    // the point at which going up the tree would leave nodes unreachable. We pick the best node out of the available
    // moves, and repeat.

    // You wouldn't believe the hassle for me to work that out, and the algorithm at the end isn't particularly
    // efficient, but as we only have 50 nodes it works out ok.

    SortedSet<Integer> cities;
    Map<Integer, SortedSet<Integer>> flightMap;
    int rootZip;

    @Override
    protected String runCase() {
        String[] parts = getLine().split(" ");
        int nCities = Integer.parseInt(parts[0]);
        int nFlights = Integer.parseInt(parts[1]);

        cities = new TreeSet<Integer>();
        Map<Integer, Integer> zipMap = new HashMap<Integer, Integer>();
        for (int i = 0; i < nCities; ++i) {
            int zip = Integer.parseInt(getLine());
            cities.add(zip);
            zipMap.put(i + 1, zip);
        }

        flightMap = new HashMap<Integer, SortedSet<Integer>>();
        for (int city : cities) {
            flightMap.put(city, new TreeSet<Integer>());
        }
        for (int i = 0; i < nFlights; ++i) {
            String[] flightParts = getLine().split(" ");
            int city1 = zipMap.get(Integer.parseInt(flightParts[0]));
            int city2 = zipMap.get(Integer.parseInt(flightParts[1]));
            flightMap.get(city1).add(city2);
            flightMap.get(city2).add(city1);
        }

        rootZip = cities.first();
        State state = new State(new State(), rootZip);

        System.err.println("xxxxxxxx");

        while (!state.pathToHere.isEmpty()) {
            System.err.println(state);
            List<State> nextStates = getSuccessors(state);
            // System.err.println(nextStates);
            state = bestSuccessor(nextStates);
        }
        System.err.println(state);

        return state.result;
    }

    private State bestSuccessor(List<State> states) {
        int i = 99999999;
        State best = null;
        for (State state : states) {
            if (state.pathToHere.isEmpty()) {
                return state; // Ended.
            }
            if (state.whereAmI() < i) {
                i = state.whereAmI();
                best = state;
            }
        }
        return best;
    }

    class State {
        public Set<Integer> visited;
        public Set<Integer> visiting;
        public List<Integer> pathToHere;
        String result;

        State() {
            visited = new HashSet<Integer>();
            visiting = new HashSet<Integer>();
            pathToHere = new ArrayList<Integer>();
            result = "";
        }

        State(State s) {
            visited = new HashSet<Integer>(s.visited);
            visiting = new HashSet<Integer>(s.visiting);
            pathToHere = new ArrayList<Integer>(s.pathToHere);
            result = s.result;
        }

        public State(State state, int child) {
            this(state);
            visiting.add(child);
            pathToHere.add(child);
            result = result + String.format("%05d", child);
        }

        void popLevel() {
            visited.add(pathToHere.get(pathToHere.size() - 1));
            pathToHere.remove(pathToHere.size() - 1);
        }

        int whereAmI() {
            return pathToHere.get(pathToHere.size() - 1);
        }

        public String toString() {
            return "Visited: " + visited + "\nVisiting: " + visiting + "\n PathToHere: " + pathToHere +
                    "\n Result: " + result;
        }
    }

    List<State> getSuccessors(State state) {
        List<State> succs = new ArrayList<State>();
        do {
            // Ah, appear to have finished?
            if (state.pathToHere.size() == 0) {
                succs.add(state);
                break;
            }

            // Add any more we can visit at this level...
            for (int child : flightMap.get(state.whereAmI())) {
                if (!state.visited.contains(child) && !state.visiting.contains(child)) {
                    succs.add(new State(state, child));
                    break;
                }
            }
            // Then try to iterate up to the next level.
            if (someNodeOnlyAccessibleThroughHere(state)) {
                break;
            }
            state.popLevel();
        } while (true);

        return succs;
    }

    boolean someNodeOnlyAccessibleThroughHere(State state) {
        Set<Integer> visited = state.visited;
        visited.add(state.whereAmI());
        return canReach(visited).size() != cities.size();
    }

    Set<Integer> canReach(Set<Integer> visited) {
        Set<Integer> reachable = new HashSet<Integer>(visited);
        canReachAux(rootZip, flightMap, reachable);
        // System.err.println("Reachable: " + reachable);
        return reachable;
    }

    private void canReachAux(int zip, Map<Integer, SortedSet<Integer>> flightMap, Set<Integer> visited) {
        if (visited.contains(zip)) {
            return;
        }
        visited.add(zip);
        Set<Integer> children = flightMap.get(zip);
        for (int child : children) {
            canReachAux(child, flightMap, visited);
        }
    }
}
