package name.arbitrary.y2014.r1a;

import name.arbitrary.CodeJamBase;

import java.util.*;

// 20:00 - 20:57, uninterrupted
public class B_FullBinaryTree extends CodeJamBase {
    B_FullBinaryTree(String fileName) {
        super(fileName);
    }

    public static void main(String[] args) {
        new B_FullBinaryTree(args[0]).run();
    }

    // A full binary tree looks like a tree where every node has either 3 or 1 connection, except one node
    // that has two connections.

    // Simplest solution: Take each node as a possible root, and then push through enforcing this.

    class Inner {
        private final int numNodes;
        private final Map<Integer, Set<Integer>> edges = new HashMap<Integer, Set<Integer>>();

        Inner() {
            numNodes = Integer.parseInt(getLine());
            for (int i = 0; i < numNodes; i++) {
                edges.put(i + 1, new HashSet<Integer>());
            }

            for (int i = 0; i < numNodes - 1; i++) {
                String parts[] = getLine().split(" ");
                assert parts.length == 2;
                int n1 = Integer.parseInt(parts[0]);
                int n2 = Integer.parseInt(parts[1]);
                edges.get(n1).add(n2);
                edges.get(n2).add(n1);
            }
        }

        public String run() {
            int minNumEdges = Integer.MAX_VALUE;
            for (int i = 1; i <= numNodes; ++i) {
                System.err.println("Doing: " + i);
                int cost = removeCost(i);
                System.err.println("Cost: " + cost);
                minNumEdges = Math.min(minNumEdges, cost);
            }

            if (minNumEdges == Integer.MAX_VALUE) {
                // Can always trim all but one node!
                minNumEdges = numNodes - 1;
            }
            return "" + minNumEdges;
        }

        public int removeCost(int i) {
            delCache = new HashMap<Integer, Integer>();
            tidyCache = new HashMap<Integer, Integer>();
            return tidyCost(i, -1);
        }

        Map<Integer, Integer> delCache;
        Map<Integer, Integer> tidyCache;

        // The cost of deleting a node and all its children, ignoring the 'ignoreEdge' edge.
        public int delCost(int node, int ignoreNode) {
            Integer res = delCache.get(node);
            if (res != null) {
                return res;
            }

            Set<Integer> neighbours = new HashSet<Integer>(edges.get(node));
            neighbours.remove(ignoreNode);
            int cost = 1; // Include ourselves.
            for (Integer neighbour : neighbours) {
                cost += delCost(neighbour, node);
            }
            delCache.put(node, cost);
            return cost;
        }

        // The cost of making the given node into a nice binary tree, ignoring the 'ignoreNode' edge.
        public int tidyCost(int node, int ignoreNode) {
            Integer res = tidyCache.get(node);
            if (res != null) {
                return res;
            }

            Set<Integer> neighbours = new HashSet<Integer>(edges.get(node));
            neighbours.remove(ignoreNode);
            // The 'saveCosts' are the costs you save by just tidying a tree, rather than deleting it.
            List<Integer> saveCosts = new ArrayList<Integer>();
            // Sum up the cost of deleting all children...
            int cost = 0;
            for (Integer neighbour : neighbours) {
                int delCostN = delCost(neighbour, node);
                int tidyCostN = tidyCost(neighbour, node);
                cost += delCostN;
                saveCosts.add(delCostN - tidyCostN);
            }

            if (saveCosts.size() >= 2) {
                // Going to be cheaper to have some children...
                Collections.sort(saveCosts);
                cost -= saveCosts.get(saveCosts.size()-1);
                cost -= saveCosts.get(saveCosts.size()-2);
            }

            tidyCache.put(node, cost);
            return cost;
        }

    }

    @Override
    protected String runCase() {
        Inner inner = new Inner();
        return inner.run();
   }


}